<template>
  <header class="uci-topbar">
    <div class="uci-topbar-inner">
      <div class="uci-wordmark">
        <svg width="28" height="28" viewBox="0 0 48 48" fill="none" aria-hidden="true">
          <rect x="8" y="10" width="32" height="28" rx="3" fill="rgba(255,210,0,0.2)" stroke="#ffd200" stroke-width="2"/>
          <path d="M15 17h12l4 5h2" stroke="#ffd200" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M15 28h18M15 33h12" stroke="#ffd200" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        FileHub &nbsp;|&nbsp; <span>UC Irvine</span>
      </div>
      <button class="lang-btn" type="button" @click="toggleLocale">{{ t("langSwitch") }}</button>
    </div>
  </header>
  <RouterView />
</template>

<script setup>
import { t, toggleLocale } from "@/i18n";
</script>
