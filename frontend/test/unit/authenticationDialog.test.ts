// @vitest-environment happy-dom
import { afterEach, describe, expect, test, vi } from "vitest";
import { flushPromises, mount } from "@vue/test-utils";
import { defineComponent } from "vue";
import TheAuthenticationDialog from "~/components/auth/TheAuthenticationDialog.vue";

const mocks = vi.hoisted(() => ({
  login: vi.fn().mockResolvedValue({ status: 200 }),
}));
vi.mock("~/store/authentication", () => ({
  useAuthStore: () => ({ actions: { login: mocks.login } }),
}));
vi.mock("~/composables/useLang", () => ({
  useLang: () => (key: string) => key,
}));
vi.mock("~/composables/useZxcvbn", () => ({
  useZxcvbn: async () => ({ toString: "" }),
}));
vi.mock("~/composables/useToasts", () => ({ useToasts: vi.fn() }));

describe("remember-me selection", () => {
  let wrapper: ReturnType<typeof mount>;

  afterEach(() => {
    wrapper?.unmount();
    mocks.login.mockClear();
  });

  test("opts in with a label click and sends the selected value on login", async () => {
    wrapper = mount(
      defineComponent({
        components: { TheAuthenticationDialog },
        template: "<Suspense><TheAuthenticationDialog /></Suspense>",
      }),
      {
        attachTo: document.body,
        global: {
          stubs: {
            FairDialog: { template: "<div><slot /></div>" },
            ForgotPasswordDialog: true,
          },
        },
      },
    );
    await flushPromises();
    const checkbox = wrapper.get<HTMLInputElement>("#rememberMe");
    expect(checkbox.element.checked).toBe(false);

    await wrapper.get('label[for="rememberMe"]').trigger("click");
    expect(checkbox.element.checked).toBe(true);

    await wrapper.get("button").trigger("click");
    expect(mocks.login).toHaveBeenCalledWith("", "", true);
  });
});
