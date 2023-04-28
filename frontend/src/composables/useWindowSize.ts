export type WindowSize = {
  width: number;
  height: number;
  isSmallerThanSm: boolean;
  isSmallerThanMd: boolean;
  isSmallerThanLg: boolean;
  isSmallerThanXl: boolean;
};

const state = reactive({
  width: window.innerWidth,
  height: window.innerHeight,
  isSmallerThanSm: window.innerWidth < 640,
  isSmallerThanMd: window.innerWidth < 768,
  isSmallerThanLg: window.innerWidth < 1024,
  isSmallerThanXl: window.innerWidth < 1280,
});

window.onresize = () => {
  state.width = window.innerWidth;
  state.height = window.innerHeight;
  state.isSmallerThanSm = window.innerWidth < 640;
  state.isSmallerThanMd = window.innerWidth < 768;
  state.isSmallerThanLg = window.innerWidth < 1024;
  state.isSmallerThanXl = window.innerWidth < 1280;
};

export const useWindowSize = () => {
  return state;
};
