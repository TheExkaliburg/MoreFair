<template>
  <div class="scroller">
    <div class="container">
      <div class="accordion">
        <div
          v-for="(version, index) in versions"
          :key="version.number"
          class="accordion-item"
        >
          <h5 class="accordion-header">
            <button
              :data-bs-target="'#collapse' + index"
              class="accordion-button collapsed"
              data-bs-toggle="collapse"
              type="button"
            >
              {{ version.number }}
              {{
                version.data.name
                  ? `[${version.data.type}]:`
                  : `[${version.data.type}]`
              }}
              {{ version.data.name }}
            </button>
          </h5>
          <div :id="'collapse' + index" class="accordion-collapse collapse">
            <div class="accordion-body">
              <div v-if="version.data.changes.rules.length > 0">
                <h4>Rule-Changes:</h4>
                <ul>
                  <li
                    v-for="change in version.data.changes.rules"
                    :key="change"
                  >
                    <strong>{{ change }}</strong>
                  </li>
                </ul>
              </div>
              <div v-if="version.data.changes.features.length > 0">
                <h4>Features:</h4>
                <ul>
                  <li
                    v-for="change in version.data.changes.features"
                    :key="change"
                  >
                    {{ change }}
                  </li>
                </ul>
              </div>
              <div v-if="version.data.changes.balancing.length > 0">
                <h4>Balancing:</h4>
                <ul>
                  <li
                    v-for="change in version.data.changes.balancing"
                    :key="change"
                  >
                    {{ change }}
                  </li>
                </ul>
              </div>
              <div v-if="version.data.changes.improvements.length > 0">
                <h4>Improvements:</h4>
                <ul>
                  <li
                    v-for="change in version.data.changes.improvements"
                    :key="change"
                  >
                    {{ change }}
                  </li>
                </ul>
              </div>
              <div v-if="version.data.changes.fixes.length > 0">
                <h4>Bugfixes:</h4>
                <ul>
                  <li
                    v-for="change in version.data.changes.fixes"
                    :key="change"
                  >
                    {{ change }}
                  </li>
                </ul>
              </div>
              <div v-if="version.data.changes.api.length > 0">
                <h4>API-Changes:</h4>
                <ul>
                  <li v-for="change in version.data.changes.api" :key="change">
                    {{ change }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useStore } from "vuex";

const store = useStore();
const versions = computed(() => store.getters["versioning/getVersions"]);
</script>

<style lang="scss" scoped>
.scroller {
  max-height: calc(100vh - 56px);
  overflow: auto !important;
}

.accordion-item {
  color: var(--text-color);
}
</style>
