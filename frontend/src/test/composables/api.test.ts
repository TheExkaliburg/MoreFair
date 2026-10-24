// @vitest-environment happy-dom
import {
  afterAll,
  beforeAll,
  beforeEach,
  describe,
  expect,
  test,
  vi,
} from "vitest";
import axios, { AxiosError } from "axios";
import type { AxiosInstance, AxiosRequestConfig } from "axios";
import type { useAPI } from "~/composables/useAPI";

const mocks = vi.hoisted(() => ({
  cookie: vi.fn(),
  notify: vi.fn(),
}));

vi.mock("js-cookie", () => ({ default: { get: mocks.cookie } }));
vi.mock("~/composables/useToasts", () => ({ useToasts: mocks.notify }));
vi.mock("~/store/chat", () => ({ ChatType: { GLOBAL: "GLOBAL" } }));

function response(config: AxiosRequestConfig, status = 200) {
  return { config, status, statusText: "", headers: {}, data: {} };
}

describe("Axios API compatibility", () => {
  const adapter = vi.fn((config: AxiosRequestConfig) =>
    Promise.resolve(response(config)),
  );
  const originalAdapter = axios.defaults.adapter;
  let client: AxiosInstance;
  let api: ReturnType<typeof useAPI>;

  beforeAll(async () => {
    axios.defaults.adapter = adapter;
    const module = await import("~/composables/useAPI");
    client = module.axiosInstance;
    api = module.useAPI();
    await vi.waitFor(() => expect(adapter).toHaveBeenCalled());
  });

  afterAll(() => {
    axios.defaults.adapter = originalAdapter;
  });

  beforeEach(() => {
    mocks.cookie.mockReset();
    mocks.notify.mockClear();
    adapter.mockReset();
    adapter.mockImplementation((config) => Promise.resolve(response(config)));
  });

  test("preserves login form encoding, credentials and the CSRF header", async () => {
    mocks.cookie.mockReturnValue("csrf-token");

    await api.auth.login("Player", "password", true);

    const config = adapter.mock.calls[0][0];
    expect(config.url).toBe("/api/auth/login");
    expect(config.withCredentials).toBe(true);
    expect(config.headers?.["X-XSRF-TOKEN"]).toBe("csrf-token");
    expect(config.data).toBe(
      "username=player&password=password&remember-me=true",
    );
    expect(config.headers?.["Content-Type"]).toContain(
      "application/x-www-form-urlencoded",
    );
  });

  test("retries a forbidden request once with a refreshed CSRF token", async () => {
    mocks.cookie.mockReturnValue("old-token");
    adapter.mockImplementationOnce((config) => {
      mocks.cookie.mockReturnValue("new-token");
      return Promise.reject(
        new AxiosError(
          "Forbidden",
          "ERR_BAD_REQUEST",
          config,
          undefined,
          response(config, 403),
        ),
      );
    });

    await client.get("/api/account");

    expect(adapter).toHaveBeenCalledTimes(2);
    const retry = adapter.mock.calls[1][0];
    expect(retry.headers?.["X-XSRF-TOKEN"]).toBe("new-token");
    expect(retry.headers?.["X-RETRY"]).toBe("true");
  });

  test("does not retry a forbidden request when the CSRF token is unchanged", async () => {
    mocks.cookie.mockReturnValue("same-token");
    adapter.mockImplementation((config) => {
      return Promise.reject(
        new AxiosError(
          "Forbidden",
          "ERR_BAD_REQUEST",
          config,
          undefined,
          response(config, 403),
        ),
      );
    });

    await expect(client.get("/api/account")).rejects.toMatchObject({
      response: { status: 403 },
    });
    expect(adapter).toHaveBeenCalledOnce();
  });

  test("does not loop when the retried request is also forbidden", async () => {
    mocks.cookie.mockReturnValue("old-token");
    adapter.mockImplementation((config) => {
      mocks.cookie.mockReturnValue(
        config.headers?.["X-RETRY"] ? "third-token" : "new-token",
      );
      return Promise.reject(
        new AxiosError(
          "Forbidden",
          "ERR_BAD_REQUEST",
          config,
          undefined,
          response(config, 403),
        ),
      );
    });

    await expect(client.get("/api/account")).rejects.toMatchObject({
      response: { status: 403 },
    });
    expect(adapter).toHaveBeenCalledTimes(2);
  });

  test("keeps external theme requests separate from authenticated API requests", async () => {
    mocks.cookie.mockReturnValue("csrf-token");

    await axios.get("https://themes.example/theme.css");

    const config = adapter.mock.calls[0][0];
    expect(config.url).toBe("https://themes.example/theme.css");
    expect(config.withCredentials).not.toBe(true);
    expect(config.headers?.["X-XSRF-TOKEN"]).toBeUndefined();
  });
});
