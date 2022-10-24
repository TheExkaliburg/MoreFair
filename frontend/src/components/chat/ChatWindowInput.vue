<template>
  <div
    class="absolute inset-x-2 bottom-1 flex flex-col"
    @keydown.down.prevent=""
    @keydown.up.prevent=""
  >
    <div v-if="input.includes('@') && filteredMentions.length > 0">
      <ul
        v-for="(s, index) in filteredMentions"
        :key="index"
        class="bg-gray-300"
      >
        <li>{{ s.name }}#{{ s.id }}</li>
      </ul>
    </div>
    <div class="flex flex-row">
      <input
        ref="inputField"
        v-model="input"
        class="basis-4/5 w-full px-2 text-black rounded-l-md"
        type="text"
      />
      <button class="h-8 basis-1/5 px-2 bg-blue-500 rounded-r-md">Send</button>
    </div>
  </div>
</template>

<script lang="ts" setup>
export type Ranker = { name: string; id: number };

const mentions: Ranker[] = [
  { name: "Kali", id: 11235 },
  { name: "Lynn", id: 6969 },
  { name: "Grapes", id: 1913 },
];

const input = ref<string>("");
const inputField = ref<HTMLInputElement | null>(null);

const filteredMentions = computed<Ranker[]>(() => {
  // filter the mentions and sort them based of the index where the mentionSearch is found
  if (!isNaN(mentionIdSearch.value)) {
    return mentions
      .filter((m) => String(m.id).includes(String(mentionIdSearch.value)))
      .sort((a, b) => {
        const aIndex = String(a.id).indexOf(String(mentionIdSearch.value));
        const bIndex = String(b.id).indexOf(String(mentionIdSearch.value));
        return aIndex !== bIndex ? aIndex - bIndex : a.id - b.id;
      });
  }

  return mentions
    .filter((s) => s.name.toLowerCase().includes(mentionSearch.value))
    .sort((a, b) => {
      const aIndex = a.name.toLowerCase().indexOf(mentionSearch.value);
      const bIndex = b.name.toLowerCase().indexOf(mentionSearch.value);
      return aIndex !== bIndex ? aIndex - bIndex : a.id - b.id;
    });
});

const mentionIdSearch = computed<number>(() => {
  if (!mentionSearch.value.startsWith("#")) {
    return NaN;
  }
  return parseInt(mentionSearch.value.split("#")[1]);
});

const mentionSearch = computed<string>(() => {
  const mentionParts = lastPreString.value.split("@");
  const lastMentionParts = mentionParts[mentionParts.length - 1];

  return lastMentionParts.toLowerCase();
});

const lastPreString = computed<string>(() => {
  if (!inputField.value) return "";

  const preString = input.value.substring(0, inputField.value.selectionStart);
  const preStringParts = preString.split(" ");
  return preStringParts[preStringParts.length - 1];
});
</script>
