<template>
  <RouterView />
  <button class="lang-btn" type="button" @click="toggleLocale">{{ t("langSwitch") }}</button>
</template>

<script setup>
import { t, toggleLocale } from "@/i18n";
</script>
