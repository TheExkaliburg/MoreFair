// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import axios, { AxiosError } from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import type { useAPI } from "~/composables/useAPI";

const mocks = vi.hoisted(() => ({ cookie: vi.fn(), notify: vi.fn() }));
vi.mock("js-cookie", () => ({ default: { get: mocks.cookie } }));
vi.mock("~/composables/useToasts", () => ({ useToasts: mocks.notify }));
vi.mock("~/store/chat", () => ({ ChatType: { GLOBAL: "GLOBAL" } }));

function response(config: InternalAxiosRequestConfig) {
  return { config, status: 200, statusText: "", headers: {}, data: true };
}

describe("session restoration before startup requests", () => {
  const adapter = vi.fn((config: InternalAxiosRequestConfig) =>
    Promise.resolve(response(config)),
  );
  const originalAdapter = axios.defaults.adapter;
  let client: AxiosInstance;
  let api: ReturnType<typeof useAPI>;

  beforeEach(async () => {
    vi.resetModules();
    mocks.cookie.mockReturnValue("old-token");
    adapter.mockReset();
    adapter.mockImplementation((config) => Promise.resolve(response(config)));
    axios.defaults.adapter = adapter;
    const module = await import("~/composables/useAPI");
    client = module.axiosInstance;
    api = module.useAPI();
  });

  afterEach(() => {
    axios.defaults.adapter = originalAdapter;
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  test("does not restore a session just by importing the API", () => {
    expect(adapter).not.toHaveBeenCalled();
  });

  test("shares restoration and waits for rotated cookies before loading game data", async () => {
    vi.stubGlobal("navigator", { locks: undefined });
    let finishRestoration: () => void = () => {
      throw new Error("Session restoration has not started");
    };
    adapter.mockImplementationOnce(
      (config) =>
        new Promise((resolve) => {
          finishRestoration = () => {
            mocks.cookie.mockReturnValue("new-token");
            resolve(response(config));
          };
        }),
    );

    const status = api.auth.authenticationStatus();
    const duplicateStatus = api.auth.authenticationStatus();
    const account = api.account.getAccountDetails();
    const csrf = client.get("/api/auth/csrf");
    await vi.waitFor(() => expect(adapter).toHaveBeenCalledOnce());
    expect(adapter.mock.calls[0]![0].url).toBe("/api/auth");

    finishRestoration();
    const results = await Promise.all([status, duplicateStatus, account, csrf]);

    expect(results[0]).toBe(results[1]);
    expect(adapter).toHaveBeenCalledTimes(3);
    for (const [config] of adapter.mock.calls.slice(1)) {
      expect(config.headers.get("X-XSRF-TOKEN")).toBe("new-token");
    }
    await api.chat.getSuggestions();
    expect(
      adapter.mock.calls.filter(([config]) => config.url === "/api/auth"),
    ).toHaveLength(1);
  });

  test("serializes restoration across tabs using Web Locks", async () => {
    let released = Promise.resolve();
    const requestLock = vi.fn(
      (_name: string, callback: () => Promise<unknown>) => {
        const result = released.then(callback);
        released = result.then(() => {});
        return result;
      },
    );
    vi.stubGlobal("navigator", { locks: { request: requestLock } });
    vi.resetModules();
    const otherTab = (await import("~/composables/useAPI")).useAPI();
    let finishRestoration: () => void = () => {
      throw new Error("Session restoration has not started");
    };
    adapter.mockImplementationOnce(
      (config) =>
        new Promise((resolve) => {
          finishRestoration = () => resolve(response(config));
        }),
    );

    const first = api.auth.authenticationStatus();
    const second = otherTab.auth.authenticationStatus();
    await vi.waitFor(() => expect(adapter).toHaveBeenCalledOnce());
    expect(requestLock).toHaveBeenCalledTimes(2);
    expect(requestLock).toHaveBeenCalledWith(
      "fair-session-initialization",
      expect.any(Function),
    );

    finishRestoration();
    await Promise.all([first, second]);
    expect(adapter).toHaveBeenCalledTimes(2);
  });

  test("propagates restoration errors without sending protected requests and allows retry", async () => {
    const error = new AxiosError("Network error", "ERR_NETWORK");
    adapter.mockRejectedValueOnce(error);
    const location = window.location.href;

    await expect(api.account.getAccountDetails()).rejects.toBe(error);
    expect(adapter).toHaveBeenCalledOnce();
    expect(window.location.href).toBe(location);

    await api.account.getAccountDetails();
    expect(adapter.mock.calls.map(([config]) => config.url)).toEqual([
      "/api/auth",
      "/api/auth",
      "/api/account",
    ]);
  });

  test("does not cache authentication status after initialization", async () => {
    adapter.mockImplementationOnce((config) =>
      Promise.resolve({ ...response(config), data: false }),
    );

    expect((await api.auth.authenticationStatus()).data).toBe(false);
    expect((await api.auth.authenticationStatus()).data).toBe(true);
    expect(adapter).toHaveBeenCalledTimes(2);
  });
});
