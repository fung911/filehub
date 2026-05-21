<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { api, downloadFile } from "@/api";
import { authState, clearSession } from "@/stores/auth";
import { t, locale } from "@/i18n";

const router = useRouter();
const files = ref([]);
const selectedFile = ref(null);
const loading = ref(false);
const uploading = ref(false);
const feedback = ref("");
const feedbackType = ref("");

const DEMO_FILES = [
  { id: 1, originalFilename: "annual-report-2024.pdf", contentType: "application/pdf", size: 2097152, uploadedAt: "2024-03-10T08:30:00Z" },
  { id: 2, originalFilename: "profile-photo.jpg", contentType: "image/jpeg", size: 512000, uploadedAt: "2024-03-12T14:22:00Z" },
  { id: 3, originalFilename: "budget-Q1.xlsx", contentType: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", size: 204800, uploadedAt: "2024-03-15T11:05:00Z" },
  { id: 4, originalFilename: "presentation.pptx", contentType: "application/vnd.openxmlformats-officedocument.presentationml.presentation", size: 3145728, uploadedAt: "2024-03-18T16:45:00Z" },
  { id: 5, originalFilename: "source-code.zip", contentType: "application/zip", size: 1048576, uploadedAt: "2024-03-20T09:10:00Z" },
];

const isDemo = computed(() => authState.token === "demo-token");
const tokenPreview = computed(() => authState.token || t("noToken"));
const selectedFileName = computed(() => selectedFile.value?.name || t("chooseFile"));

function setFeedback(message, type = "") {
  feedback.value = message;
  feedbackType.value = type;
}

function formatBytes(bytes) {
  if (bytes === 0) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  const value = bytes / Math.pow(1024, index);
  return `${value.toFixed(value >= 10 || index === 0 ? 0 : 1)} ${units[index]}`;
}

function formatDate(value) {
  return new Intl.DateTimeFormat(locale.value === "zh" ? "zh-CN" : "en-US", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

async function loadFiles() {
  if (isDemo.value) {
    files.value = DEMO_FILES;
    return;
  }

  loading.value = true;
  try {
    files.value = await api("/api/files");
  } catch (error) {
    setFeedback(error.message, "error");
    if (!authState.token) {
      await router.push("/login");
    }
  } finally {
    loading.value = false;
  }
}

function chooseFile(event) {
  selectedFile.value = event.target.files[0] || null;
}

async function upload() {
  if (!selectedFile.value) return;

  if (isDemo.value) {
    setFeedback(t("demoUpload"), "error");
    return;
  }

  uploading.value = true;
  const data = new FormData();
  data.append("file", selectedFile.value);

  try {
    await api("/api/files", {
      method: "POST",
      body: data
    });
    selectedFile.value = null;
    document.querySelector("#fileInput").value = "";
    setFeedback(t("uploadSuccess"), "success");
    await loadFiles();
  } catch (error) {
    setFeedback(error.message, "error");
  } finally {
    uploading.value = false;
  }
}

async function handleDownload(file) {
  if (isDemo.value) {
    setFeedback(t("demoDownload"), "error");
    return;
  }

  try {
    await downloadFile(file);
  } catch (error) {
    setFeedback(error.message, "error");
  }
}

async function copyToken() {
  await navigator.clipboard.writeText(authState.token);
  setFeedback(t("tokenCopied"), "success");
}

async function logout() {
  clearSession();
  await router.push("/login");
}

onMounted(loadFiles);
</script>

<template>
  <main class="files-page">
    <aside class="side-panel">
      <div class="brand-row">
        <div class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 48 48" role="img">
            <rect x="8" y="10" width="32" height="28" rx="5" />
            <path d="M15 17h12l4 5h2" />
            <path d="M15 28h18M15 33h12" />
          </svg>
        </div>
        <div>
          <p class="eyebrow">FileHub</p>
          <h1>{{ t("fileManager") }}</h1>
        </div>
      </div>

      <div class="status-strip logged-in">
        <span class="status-dot" aria-hidden="true"></span>
        <span>{{ t("loggedIn") }}{{ authState.username }}</span>
      </div>

      <div class="token-box">
        <div class="token-head">
          <span>Token</span>
          <button class="icon-btn" type="button" :title="t('copyToken')" :aria-label="t('copyToken')" @click="copyToken">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <rect x="8" y="8" width="10" height="12" rx="2" />
              <path d="M6 16H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
            </svg>
          </button>
        </div>
        <code>{{ tokenPreview }}</code>
      </div>

      <button class="secondary-btn" type="button" @click="logout">{{ t("logout") }}</button>
    </aside>

    <section class="file-panel">
      <header class="file-header">
        <div>
          <p class="eyebrow">Files</p>
          <h2>{{ t("myUploads") }}</h2>
        </div>
        <button class="secondary-btn compact" type="button" :disabled="loading" @click="loadFiles">{{ t("refresh") }}</button>
      </header>

      <form class="upload-area" @submit.prevent="upload">
        <input id="fileInput" type="file" required @change="chooseFile">
        <label class="upload-drop" for="fileInput">
          <span class="upload-icon" aria-hidden="true">
            <svg viewBox="0 0 48 48" role="img">
              <path d="M24 34V14" />
              <path d="m16 22 8-8 8 8" />
              <rect x="9" y="32" width="30" height="7" rx="2" />
            </svg>
          </span>
          <span class="upload-title">{{ selectedFileName }}</span>
          <span class="upload-meta">{{ t("maxSize") }}</span>
        </label>
        <button class="primary-btn" type="submit" :disabled="uploading || !selectedFile">
          {{ uploading ? t("uploading") : t("upload") }}
        </button>
      </form>

      <p v-if="feedback" class="feedback" :class="feedbackType">{{ feedback }}</p>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ t("colName") }}</th>
              <th>{{ t("colType") }}</th>
              <th>{{ t("colSize") }}</th>
              <th>{{ t("colDate") }}</th>
              <th>{{ t("colAction") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!files.length" class="empty-row">
              <td colspan="5">{{ loading ? t("loading") : t("noFiles") }}</td>
            </tr>
            <tr v-for="file in files" :key="file.id">
              <td><div class="file-name" :title="file.originalFilename">{{ file.originalFilename }}</div></td>
              <td><span class="pill" :title="file.contentType || 'unknown'">{{ file.contentType || "unknown" }}</span></td>
              <td>{{ formatBytes(file.size) }}</td>
              <td>{{ formatDate(file.uploadedAt) }}</td>
              <td>
                <button class="download-link" type="button" @click="handleDownload(file)">{{ t("download") }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>
