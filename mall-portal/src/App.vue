<template>
  <el-config-provider namespace="el" :message="{ max: 1 }">
    <router-view />
  </el-config-provider>
</template>
<script setup></script>
