<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "@/api";
import { saveSession } from "@/stores/auth";
import { t } from "@/i18n";

const router = useRouter();
const mode = ref("login");
const username = ref("");
const password = ref("");
const loading = ref(false);
const feedback = ref("");
const feedbackType = ref("");

const isLogin = computed(() => mode.value === "login");
const title = computed(() => (isLogin.value ? t("loginTitle") : t("registerTitle")));
const buttonText = computed(() => (isLogin.value ? t("btnLogin") : t("btnRegister")));

function switchMode(nextMode) {
  mode.value = nextMode;
  feedback.value = "";
  feedbackType.value = "";
}

async function submit() {
  feedback.value = "";
  feedbackType.value = "";
  loading.value = true;

  if (isLogin.value && username.value.trim() === "admin" && password.value === "admin") {
    saveSession("demo-token", "admin");
    await router.push("/files");
    loading.value = false;
    return;
  }

  try {
    const payload = JSON.stringify({
      username: username.value.trim(),
      password: password.value
    });

    if (isLogin.value) {
      const result = await api("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: payload
      });
      saveSession(result.token, username.value.trim());
      await router.push("/files");
      return;
    }

    await api("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: payload
    });
    feedback.value = t("registerSuccess");
    feedbackType.value = "success";
    switchMode("login");
  } catch (error) {
    feedback.value = error.message;
    feedbackType.value = "error";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-card">
      <div class="brand-row">
        <div class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 48 48" role="img">
            <rect x="8" y="10" width="32" height="28" rx="5" />
            <path d="M15 17h12l4 5h2" />
            <path d="M15 28h18M15 33h12" />
          </svg>
        </div>
        <div>
          <p class="eyebrow">Spring Boot + Vue</p>
          <h1>FileHub Auth</h1>
        </div>
      </div>

      <div class="segmented" role="tablist" :aria-label="t('authMode')">
        <button
          class="segment"
          :class="{ active: isLogin }"
          type="button"
          role="tab"
          :aria-selected="String(isLogin)"
          @click="switchMode('login')"
        >
          {{ t("loginTab") }}
        </button>
        <button
          class="segment"
          :class="{ active: !isLogin }"
          type="button"
          role="tab"
          :aria-selected="String(!isLogin)"
          @click="switchMode('register')"
        >
          {{ t("registerTab") }}
        </button>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <h2>{{ title }}</h2>
        <label>
          {{ t("username") }}
          <input v-model.trim="username" autocomplete="username" minlength="3" maxlength="64" required>
        </label>
        <label>
          {{ t("password") }}
          <input
            v-model="password"
            type="password"
            :autocomplete="isLogin ? 'current-password' : 'new-password'"
            :minlength="isLogin ? undefined : 6"
            maxlength="100"
            required
          >
        </label>
        <button class="primary-btn" type="submit" :disabled="loading">
          {{ loading ? t("processing") : buttonText }}
        </button>
      </form>

      <p v-if="feedback" class="feedback" :class="feedbackType">{{ feedback }}</p>
      <p class="demo-hint">{{ t("demoHint") }}</p>
    </section>
  </main>
</template>
