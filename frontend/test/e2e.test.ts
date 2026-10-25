import { createServer, type Server } from "node:http";
import { readFile } from "node:fs/promises";
import { extname, resolve, sep } from "node:path";
import { chromium, type Browser, type Page } from "playwright";
import { afterAll, beforeAll, describe, expect, test } from "vitest";

const output = resolve(import.meta.dirname, "../.output/public");
const contentTypes: Record<string, string> = {
  ".html": "text/html",
  ".js": "text/javascript",
  ".css": "text/css",
  ".json": "application/json",
  ".txt": "text/plain",
  ".wasm": "application/wasm",
  ".png": "image/png",
  ".ico": "image/x-icon",
};
let server: Server;
let browser: Browser;
let origin: string;

beforeAll(async () => {
  await readFile(resolve(output, "index.html"));
  server = createServer(async (request, response) => {
    const path = decodeURIComponent(
      new URL(request.url!, "http://localhost").pathname,
    );
    const file = resolve(output, `.${path}`);
    if (file !== output && !file.startsWith(output + sep)) {
      response.writeHead(403).end();
      return;
    }
    try {
      const body = await readFile(
        extname(file) ? file : resolve(file, "index.html"),
      );
      response
        .writeHead(200, {
          "Content-Type": contentTypes[extname(file)] ?? "text/html",
        })
        .end(body);
    } catch (error) {
      if (
        !(error instanceof Error) ||
        !("code" in error) ||
        error.code !== "ENOENT"
      ) {
        response.writeHead(500).end(String(error));
        return;
      }
      if (extname(file)) {
        response.writeHead(404).end();
        return;
      }
      response
        .writeHead(200, { "Content-Type": "text/html" })
        .end(await readFile(resolve(output, "200.html")));
    }
  });
  await new Promise<void>((done) => server.listen(0, "127.0.0.1", done));
  const address = server.address();
  if (!address || typeof address === "string")
    throw new Error("Missing static server address");
  origin = `http://127.0.0.1:${address.port}`;
  browser = await chromium.launch({ headless: true });
});

afterAll(async () => {
  await browser?.close();
  if (server) await new Promise<void>((done) => server.close(() => done()));
});

async function openPage() {
  const page = await browser.newPage();
  await page.route("**/*", async (route) => {
    const url = new URL(route.request().url());
    if (url.origin !== origin || url.pathname.startsWith("/api/")) {
      if (url.origin === origin && url.pathname === "/api/auth") {
        await route.fulfill({ json: false });
      } else {
        await route.abort();
      }
      return;
    }
    await route.continue();
  });
  await page.routeWebSocket("**/*", (socket) => socket.close());
  return page;
}

async function expectContent(page: Page, path: string) {
  await expect.poll(() => new URL(page.url()).pathname).toBe(path);
  const visible = page.locator("main.md-content");
  await visible.waitFor();
  await expect
    .poll(() => visible.innerText(), { timeout: 15000 })
    .not.toMatch(/Loading\.\.\.|404:/);
  const markdown = await readFile(
    resolve(import.meta.dirname, `../src/content${path}.md`),
    "utf8",
  );
  const heading = markdown.match(/^# (.+)$/m)?.[1];
  if (!heading) throw new Error(`Missing Markdown heading for ${path}`);
  await expect.poll(() => visible.locator("h1").innerText()).toBe(heading);
  expect(await page.title()).toContain(heading);
  expect((await visible.innerText()).length).toBeGreaterThan(30);
}

describe("Frontend end-to-end smoke tests against generated files", () => {
  test.each(["rules", "privacy", "impressum", "contributions", "changelog"])(
    "renders /%s without a Nuxt API",
    async (slug) => {
      const page = await openPage();
      try {
        await page.goto(`${origin}/${slug}`);
        await expectContent(page, `/${slug}`);
        expect(
          await page.locator("main.md-content").getAttribute("class"),
        ).toContain("md-content");
      } finally {
        await page.close();
      }
    },
  );

  test("updates content on client navigation and preserves not-found behavior", async () => {
    const page = await openPage();
    try {
      await page.goto(`${origin}/rules`);
      await expectContent(page, "/rules");
      expect(
        await page
          .locator('main a[href="https://discord.gg/ThKzCknfFr"]')
          .count(),
      ).toBe(1);
      await page.evaluate(() => {
        Reflect.set(window, "__navigationMarker", true);
      });
      // Use the app's existing NuxtLink rather than reloading the document.
      await page
        .getByRole("button", { name: "Open Main Menu" })
        .first()
        .click();
      await page.locator('a[href="/privacy"]:visible').first().click();
      await expectContent(page, "/privacy");
      expect(await page.locator("main h2 a").first().getAttribute("href")).toBe(
        "#_1-definitions",
      );
      expect(
        await page.evaluate(() => Reflect.get(window, "__navigationMarker")),
      ).toBe(true);
      await page.goto(`${origin}/does-not-exist`);
      await expect
        .poll(() => page.locator("main").innerText())
        .toContain("404: Cannot find the page you are looking for.");
    } finally {
      await page.close();
    }
  });

  test("reports a failed content database request instead of a missing page", async () => {
    const page = await openPage();
    try {
      await page.route("**/__nuxt_content/**", (route) =>
        route.fulfill({ status: 500, body: "Content unavailable" }),
      );
      await page.goto(`${origin}/rules`);
      await expect
        .poll(() => page.locator("body").innerText(), { timeout: 15000 })
        .toMatch(/500|error|failed/i);
      expect(await page.locator("body").innerText()).not.toContain(
        "404: Cannot find the page you are looking for.",
      );
    } finally {
      await page.close();
    }
  });

  test("serves public assets and preserves Windi theme/layout utilities", async () => {
    const page = await openPage();
    try {
      await page.goto(`${origin}/rules`);
      await expectContent(page, "/rules");
      for (const asset of [
        "/favicon.ico",
        "/img/grapeDoodle.png",
        "/sounds/mention.wav",
      ]) {
        const response = await page.request.get(origin + asset);
        expect(response.ok()).toBe(true);
      }
      const styles = await page.evaluate(() => {
        const button = document.createElement("button");
        button.className =
          "border-1 border-solid h-8 bg-button-bg text-button-text";
        document.body.appendChild(button);
        const style = getComputedStyle(button);
        return {
          border: style.borderTopWidth,
          height: style.height,
          background: style.backgroundColor,
        };
      });
      expect(styles.border).toBe("1px");
      expect(styles.height).toBe("32px");
      expect(styles.background).not.toBe("rgba(0, 0, 0, 0)");
      expect(
        (await page.locator("main.md-content").boundingBox())?.width,
      ).toBeGreaterThan(200);
      expect(
        await page
          .locator("main .w-reader")
          .evaluate((element) => getComputedStyle(element).width),
      ).toBe("800px");
      await page.setViewportSize({ width: 375, height: 812 });
      expect(
        (await page.locator("main .w-reader").boundingBox())?.width,
      ).toBeLessThanOrEqual(375);
    } finally {
      await page.close();
    }
  });

  test("restores the saved Light theme without resetting options", async () => {
    const page = await openPage();
    try {
      await page.addInitScript(() => {
        localStorage.setItem(
          "options",
          JSON.stringify({
            theme: { themeSelector: { value: "Light" } },
            ladder: { showEta: { value: false } },
          }),
        );
      });
      await page.goto(`${origin}/rules`);
      await expectContent(page, "/rules");
      expect(await page.locator("html").getAttribute("class")).toBe("light");
      expect(
        await page
          .locator("html")
          .evaluate((element) => getComputedStyle(element).backgroundColor),
      ).not.toBe("rgb(16, 20, 31)");
      const saved = await page.evaluate(() =>
        JSON.parse(localStorage.getItem("options")!),
      );
      expect(saved.ladder.showEta.value).toBe(false);
      expect(saved.theme.themeSelector.value).toBe("Light");
    } finally {
      await page.close();
    }
  });
});
