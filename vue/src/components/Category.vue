<template>
  <div class="card">
    <h1 class="category-name">
      <div @click="emitAlert">{{ category.categoryName }}</div>
    </h1>
  </div>
</template>

<script>
export default {
  name: "category",
  props: {
    category: Object,
  },
  methods: {
    emitAlert() {
      this.$emit("show-alert");
    },
  },
};
</script>

<style scoped>
@media screen and (min-width: 150px) {
  .category-name {
    color: white;
    padding: 0.3rem;
  }
  .card {
    min-height: 6rem;
    text-align: center;
    height: 6rem;
    margin: 0.5rem;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 15rem;
    background: linear-gradient(to bottom, #427aa1, #463a7d);
    border-radius: 10px;
  }
}
@media screen and (min-width: 400px) {
}
@media screen and (min-width: 658px) {
  .card {
    min-height: 10rem;
    width: 20rem;
  }
}
</style>