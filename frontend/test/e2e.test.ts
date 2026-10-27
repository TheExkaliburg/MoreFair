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
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
  const address = server.address();
  if (!address || typeof address === "string")
    throw new Error("Missing static server address");
  origin = `http://127.0.0.1:${address.port}`;
  browser = await chromium.launch({ headless: true });
});

afterAll(async () => {
  await browser?.close();
  if (server)
    await new Promise<void>((resolve) => server.close(() => resolve()));
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

async function openChatPage(message = "") {
  const page = await openPage();
  const errors: string[] = [];
  const sent: { destination: string; body: string }[] = [];
  const unexpectedRequests: string[] = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.route("**/api/**", async (route) => {
    const url = new URL(route.request().url());
    if (url.origin !== origin) {
      await route.abort();
      return;
    }
    let json: unknown;
    switch (url.pathname) {
      case "/api/auth":
        json = true;
        break;
      case "/api/auth/csrf":
        json = { headerName: "X-XSRF-TOKEN", token: "test-token" };
        break;
      case "/api/account":
        json = {
          accessRole: "PLAYER",
          accountId: 42,
          username: "Mystery Guest",
          email: "",
          highestCurrentLadder: 1,
          uuid: "00000000-0000-4000-8000-000000000042",
          settings: { vinegarSplit: 50 },
        };
        break;
      case "/api/chat/suggestions":
        json = [
          { displayName: "Example", accountId: 123 },
          { displayName: "Second", accountId: 124 },
          { displayName: "Mystery Guest", accountId: 42 },
        ];
        break;
      case "/api/chat/global":
      case "/api/chat/ladder":
      case "/api/chat/system":
      case "/api/chat/mod":
        json = {
          type: url.pathname.split("/").at(-1)!.toUpperCase(),
          number: 1,
          messages:
            message && url.pathname === "/api/chat/global"
              ? [
                  {
                    accountId: 123,
                    username: "Example",
                    message,
                    metadata: "[]",
                    timestamp: Date.now() - 60000,
                    chatType: "GLOBAL",
                  },
                ]
              : [],
        };
        break;
      case "/api/ladder":
        json = {
          rankers: [],
          number: 1,
          scaling: 1,
          types: ["DEFAULT"],
          basePointsToPromote: "1000",
        };
        break;
      case "/api/round":
        json = {
          settings: {},
          assholeLadder: 20,
          autoPromoteLadder: 1,
          topLadder: 1,
          types: ["DEFAULT"],
          number: 1,
        };
        break;
      case "/api/grapes/vinegar/throw/record":
      case "/api/user":
        json = [];
        break;
      default:
        unexpectedRequests.push(url.pathname);
        await route.fulfill({ status: 501, body: "Unmocked test API" });
        return;
    }
    await route.fulfill({ json });
  });
  await page.routeWebSocket("**/*", (socket) => {
    socket.onMessage((message) => {
      const frame = message.toString();
      if (frame.startsWith("CONNECT\n")) {
        socket.send("CONNECTED\nversion:1.2\nheart-beat:0,0\n\n\0");
      } else if (frame.startsWith("SEND\n")) {
        const destination = frame.match(/^destination:(.+)$/m)?.[1];
        if (!destination) throw new Error("Missing STOMP destination");
        sent.push({
          destination,
          body: frame.slice(frame.indexOf("\n\n") + 2).replace(/\0$/, ""),
        });
      }
    });
  });
  const suggestions = page.waitForResponse("**/api/chat/suggestions");
  await page.goto(origin);
  await suggestions;
  await page.locator(".ProseMirror").waitFor();
  return { page, errors, sent, unexpectedRequests };
}

describe("Chat editor against generated static files and mocked transports", () => {
  test("keeps pagination uniform, icon buttons circular, and disabled button dividers visible", async () => {
    const { page } = await openChatPage();
    try {
      for (const width of [375, 1280]) {
        await page.setViewportSize({ width, height: 900 });
        const pagination = page
          .getByRole("button", { name: "dropdown-menu", exact: true })
          .first()
          .locator("..");
        const widths = await pagination
          .locator(":scope > button")
          .evaluateAll((buttons) =>
            buttons.map((button) => button.getBoundingClientRect().width),
          );
        expect(widths).toHaveLength(6);
        for (const width of widths) expect(width).toBeCloseTo(28, 0);

        for (const name of ["help", "vinegarSettings"]) {
          const button = page.locator(`[data-tutorial="${name}"]`);
          const geometry = await button.evaluate((element) => {
            const box = element.getBoundingClientRect();
            const style = getComputedStyle(element);
            return {
              width: box.width,
              height: box.height,
              radius: parseFloat(style.borderRadius),
              padding: style.padding,
            };
          });
          expect(geometry.width).toBe(32);
          expect(geometry.height).toBe(32);
          expect(geometry.radius).toBeGreaterThanOrEqual(16);
          expect(geometry.padding).toBe("0px");
        }
        const tutorialIcon = await page
          .locator('[data-tutorial="help"] svg')
          .boundingBox();
        expect(tutorialIcon?.width).toBe(30);
        expect(tutorialIcon?.height).toBe(30);

        for (const name of ["multi", "autoPromote"]) {
          const left = page.locator(`[data-tutorial="${name}"]`);
          expect(await left.isDisabled()).toBe(true);
          const borders = await left.locator("..").evaluate((element) =>
            [...element.children].map((button) => {
              const style = getComputedStyle(button);
              return {
                left: style.borderLeftWidth,
                right: style.borderRightWidth,
              };
            }),
          );
          expect(borders[0]!.right).toBe("0px");
          expect(borders[1]!.left).toBe("1px");
        }
      }
    } finally {
      await page.close();
    }
  });

  test("avoids a mobile chat spacer and native focus rings on text editors", async () => {
    const { page } = await openChatPage("Oldest test message");
    try {
      await page.setViewportSize({ width: 375, height: 812 });
      const messages = page.locator(
        '[class~="overflow-y-auto"][class~="z-[1]"]',
      );
      const firstMessage = messages
        .locator(":scope > div")
        .filter({ hasText: "Oldest test message" });
      await firstMessage.waitFor();
      const container = await messages.boundingBox();
      const first = await firstMessage.boundingBox();
      const padding = await messages.evaluate((element) =>
        parseFloat(getComputedStyle(element).paddingTop),
      );
      if (!container || !first) throw new Error("Missing chat messages");
      expect(first.y - container.y).toBeCloseTo(padding, 0);

      const editor = page.locator(".ProseMirror");
      await editor.focus();
      expect(
        await editor.evaluate(
          (element) => getComputedStyle(element).outlineStyle,
        ),
      ).not.toBe("auto");
      await editor.pressSequentially("Still editable");
      expect(await editor.innerText()).toBe("Still editable");

      await page.goto(`${origin}/account`);
      await page
        .getByRole("button", { name: "Edit", exact: true })
        .first()
        .click();
      const input = page.locator("input").first();
      await input.focus();
      expect(
        await input.evaluate(
          (element) => getComputedStyle(element).outlineStyle,
        ),
      ).not.toBe("auto");
    } finally {
      await page.close();
    }
  });

  test("centers editable option lists and suppresses their native text input focus ring", async () => {
    const { page } = await openChatPage();
    try {
      await page.goto(`${origin}/options`);
      for (const width of [375, 1280]) {
        await page.setViewportSize({ width, height: 900 });
        for (const name of [
          "Subscribed mentions:",
          "Ignored rankers (by ranker id):",
          "Upload Custom Theme:",
        ]) {
          const label = page.getByText(name, { exact: true });
          await label.waitFor();
          const list = label.locator("..").locator(":scope > div").nth(1);
          const offsets = await list.evaluate((element) => {
            const parent = element.parentElement!.getBoundingClientRect();
            return [...element.children].map((row) => {
              const box = row.getBoundingClientRect();
              return box.x + box.width / 2 - (parent.x + parent.width / 2);
            });
          });
          for (const offset of offsets)
            expect(Math.abs(offset)).toBeLessThan(1);
          const input = list.locator("input");
          await input.focus();
          expect(
            await input.evaluate(
              (element) => getComputedStyle(element).outlineStyle,
            ),
          ).not.toBe("auto");
        }
      }
    } finally {
      await page.close();
    }
  });

  test("preserves responsive game proportions and sidebar visibility", async () => {
    const { page, errors, unexpectedRequests } = await openChatPage();
    try {
      const ladder = page.locator('[class~="lg:w-[70%]"]');
      const chat = page.locator('[class~="lg:w-[30%]"]');
      const sidebar = page.locator('[class~="max-[640px]:hidden"]').first();
      const ladderDetails = page.locator('[class~="max-[1280px]:hidden"]');
      for (const width of [375, 640, 641, 1024, 1280, 1281]) {
        await page.setViewportSize({ width, height: 900 });
        const ladderBox = await ladder.boundingBox();
        const chatBox = await chat.boundingBox();
        if (!ladderBox || !chatBox) throw new Error("Missing game panels");
        if (width < 1024) {
          expect(ladderBox.width).toBeCloseTo(chatBox.width, 0);
          expect(chatBox.y).toBeCloseTo(ladderBox.y + ladderBox.height, 0);
        } else {
          expect(ladderBox.width / chatBox.width).toBeCloseTo(7 / 3, 2);
          expect(chatBox.x).toBeCloseTo(ladderBox.x + ladderBox.width, 0);
        }
        expect(
          await sidebar.evaluate(
            (element) => getComputedStyle(element).display,
          ),
        ).toBe(width <= 640 ? "none" : "block");
        expect(
          await ladderDetails.evaluate(
            (element) => getComputedStyle(element).display,
          ),
        ).toBe(width <= 1280 ? "none" : "flex");
        expect(
          await page
            .locator('[class~="lg:h-[3.25rem]"]')
            .evaluate((element) => getComputedStyle(element).height),
        ).toBe(width < 1024 ? "48px" : "52px");
      }
      expect(errors).toEqual([]);
      expect(unexpectedRequests).toEqual([]);
    } finally {
      await page.close();
    }
  });

  test.each(["Enter", "Tab", "Control+Space", "click"])(
    "accepts a user mention with %s without sending it prematurely",
    async (selection) => {
      const { page, errors, sent, unexpectedRequests } = await openChatPage();
      try {
        const editor = page.locator(".ProseMirror");
        await editor.pressSequentially("@Ex");
        const option = page.locator("[data-tippy-root] a").filter({
          hasText: "Example#123",
        });
        await option.waitFor();
        if (selection === "click") {
          await option.click();
        } else {
          await editor.press(selection);
        }
        await expect
          .poll(() => editor.locator(".mention").innerText())
          .toBe("@Example#123");
        expect(sent).toHaveLength(0);
        await editor.press("Enter");
        await expect.poll(() => sent.length).toBe(1);
        expect(sent[0]!.destination).toBe("/app/chat/global");
        expect(JSON.parse(sent[0]!.body)).toEqual({
          content: "{@} ",
          metadata: JSON.stringify([{ u: "Example", i: 0, id: 123 }]),
        });
        await expect.poll(() => editor.textContent()).toBe("");
        expect(await editor.locator("p").getAttribute("data-placeholder")).toBe(
          "Chad is listening...",
        );
        expect(errors).toEqual([]);
        expect(unexpectedRequests).toEqual([]);
      } finally {
        await page.close();
      }
    },
  );

  test("preserves emoji/group/ID mentions", async () => {
    const { page, errors, sent, unexpectedRequests } = await openChatPage();
    try {
      const editor = page.locator(".ProseMirror");
      await editor.pressSequentially("@");
      await page.locator("[data-tippy-root] a").first().waitFor();
      expect(await page.locator("[data-tippy-root]").innerText()).not.toContain(
        "Mystery Guest",
      );
      await editor.pressSequentially("#4");
      const guest = page
        .locator("[data-tippy-root] a")
        .filter({ hasText: "Mystery Guest#42" });
      await guest.waitFor();
      await editor.press("Escape");
      await editor.press("ControlOrMeta+a");
      await editor.press("Backspace");
      await editor.pressSequentially(":smi");
      const emojiOption = page.locator("[data-tippy-root] a").first();
      await emojiOption.waitFor();
      await emojiOption.click();
      const emoji = await editor.locator(".mention").innerText();
      await editor.pressSequentially("$mo");
      await page
        .locator("[data-tippy-root] a")
        .filter({ hasText: "$mods$" })
        .waitFor();
      await editor.press("ArrowDown");
      await editor.press("ArrowUp");
      await editor.press("Enter");
      await expect
        .poll(() => editor.locator(".mention").allTextContents())
        .toEqual([emoji, "$mods$"]);
      expect(sent).toHaveLength(0);
      await page.getByRole("button", { name: "Send", exact: true }).click();
      await expect.poll(() => sent.length).toBe(1);
      expect(JSON.parse(sent[0]!.body)).toEqual({
        content: `${emoji} {$} `,
        metadata: JSON.stringify([{ g: "mods", i: emoji.length + 1 }]),
      });
      await page.goto(`${origin}/rules`);
      await expectContent(page, "/rules");
      expect(errors).toEqual([]);
      expect(unexpectedRequests).toEqual([]);
    } finally {
      await page.close();
    }
  });
});

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

  test("serves public assets and preserves Tailwind theme/layout utilities", async () => {
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
        button.className = "fair-button";
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
      expect(
        await page.locator("main h1").evaluate((element) => {
          const style = getComputedStyle(element);
          return {
            fontSize: style.fontSize,
            fontWeight: style.fontWeight,
            marginTop: style.marginTop,
          };
        }),
      ).toEqual({ fontSize: "32px", fontWeight: "700", marginTop: "21.44px" });
      expect(
        await page
          .locator("main ol")
          .first()
          .evaluate((element) => getComputedStyle(element).listStyleType),
      ).toBe("decimal");
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
