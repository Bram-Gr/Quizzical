<template>
  <h1>Logout</h1>
</template>

<script>
export default {
  created() {
    this.$store.commit("LOGOUT");
    this.$router.push("/select-page");
    window.scrollTo(0, 0);
  },
};
</script>
