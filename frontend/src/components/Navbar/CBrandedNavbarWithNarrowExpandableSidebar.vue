<template>
  <nav class="c_bg-color-main">
    <div class="mx-auto px-4">
      <div class="flex h-10 items-center justify-between">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <!--FairGame Logo and Branding-->
            <NuxtLink to="/" class="flex">
              <img src="/favicon.ico" alt="FairGame" class="w-7 h-7" />
              <span class="c_text-color-main text-xl">FairGame</span>
            </NuxtLink>
          </div>
        </div>
        <div class="-mr-2 flex md:hidden">
          <button class="">
            <Bars3Icon class="w-7 h-7 c_text-color-main" />
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { Bars3Icon } from "@heroicons/vue/24/outline";
</script>

<style scoped lang="scss"></style>
