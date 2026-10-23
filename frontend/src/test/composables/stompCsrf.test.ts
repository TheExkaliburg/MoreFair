// @vitest-environment happy-dom
import { beforeEach, describe, expect, test, vi } from "vitest";
import type { Client } from "@stomp/stompjs";
import "~/composables/useStomp";

const mocks = vi.hoisted(() => {
  const clients: Client[] = [];
  return { clients, get: vi.fn(), notify: vi.fn() };
});

vi.mock("@stomp/stompjs", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@stomp/stompjs")>();
  return {
    ...actual,
    Client: class extends actual.Client {
      constructor(config: ConstructorParameters<typeof actual.Client>[0]) {
        super(config);
        mocks.clients.push(this);
      }
    },
  };
});

vi.mock("~/composables/useAPI", () => ({
  axiosInstance: { get: mocks.get },
}));
vi.mock("~/composables/useToasts", () => ({ useToasts: mocks.notify }));
vi.mock("~/store/account", () => ({ useAccountStore: vi.fn() }));
vi.mock("~/store/chat", () => ({
  useChatStore: vi.fn(),
  ChatType: { GLOBAL: "GLOBAL" },
}));

describe("STOMP CSRF connection headers", () => {
  beforeEach(() => {
    mocks.get.mockReset();
    mocks.notify.mockClear();
    mocks.clients[0].connectHeaders = {};
  });

  test("fetches and sends the server-provided masked token", async () => {
    mocks.get.mockResolvedValue({
      data: { headerName: "X-XSRF-TOKEN", token: "masked-token" },
    });
    const client = mocks.clients[0];

    await client.beforeConnect(client);

    expect(mocks.get).toHaveBeenCalledWith("/api/auth/csrf", {
      timeout: 10000,
    });
    expect(client.connectHeaders).toEqual({ "X-XSRF-TOKEN": "masked-token" });
  });

  test("refreshes the token before each new connection", async () => {
    mocks.get
      .mockResolvedValueOnce({
        data: { headerName: "X-XSRF-TOKEN", token: "first-token" },
      })
      .mockResolvedValueOnce({
        data: { headerName: "X-XSRF-TOKEN", token: "second-token" },
      });
    const client = mocks.clients[0];

    await client.beforeConnect(client);
    expect(client.connectHeaders).toEqual({ "X-XSRF-TOKEN": "first-token" });
    await client.beforeConnect(client);
    expect(client.connectHeaders).toEqual({ "X-XSRF-TOKEN": "second-token" });
  });

  test("stops connecting and notifies the player if token fetching fails", async () => {
    mocks.get.mockRejectedValue(new Error("Network error"));
    const client = mocks.clients[0];
    const deactivate = vi.spyOn(client, "deactivate").mockResolvedValue();

    await client.beforeConnect(client);

    expect(deactivate).toHaveBeenCalledOnce();
    expect(client.connectHeaders).toEqual({});
    expect(mocks.notify).toHaveBeenCalledWith(
      "Could not establish a secure game connection. Please reload the page to try again.",
      { type: "error" },
    );
    deactivate.mockRestore();
  });
});
