<script setup>
import { ref, onMounted } from 'vue'
import HelloWorld from './components/HelloWorld.vue'
import TheWelcome from './components/TheWelcome.vue'

const greeting = ref('載入中...')
async function fetchHello() {
  try {
    // 若後端不在同來源，請改為完整 URL (例如 http://localhost:8081/hello)
    //const res = await fetch('http://localhost:8081/hello')
    const res = await fetch('hello')
    if (!res.ok) throw new Error(res.statusText)
    const text = await res.text()
    greeting.value = text
  } catch (e) {
    console.error(e)
    greeting.value = '取得失敗'
  }
}

onMounted(fetchHello)
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="./assets/logo.svg" width="125" height="125" />

    <div class="wrapper">
      <HelloWorld :msg="greeting" />
    </div>
  </header>

  <main>
    <TheWelcome />
  </main>
</template>

<style scoped>
header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>

