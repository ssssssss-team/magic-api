<template>
  <div class="ma-loading-wrapper">
    <div class="ma-loading">
      <div class="ma-title">
        <span>L</span>
        <span>o</span>
        <span>a</span>
        <span>d</span>
        <span>i</span>
        <span>n</span>
        <span>g</span>
      </div>
      <div class="ma-loading-text">By {{ title }} {{ version }}</div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'MagicLoading',
  props: {
    title: {
      type: String
    },
    version: {
      type: String
    },
  },
}
</script>
<style scoped>
.ma-loading-wrapper {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 9999999;
  text-align: center;
  background: #fff;
}

.ma-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 500px;
  height: 100px;
  margin-left: -250px;
  margin-top: -50px;
  text-align: center;
}

.ma-loading .ma-title {
  font-size: 0;
  color: #0075ff;
  letter-spacing: 0;
}

.ma-loading .ma-title label {
  font-size: 14px;
  display: inline-block;
  margin-top: 5px;
  vertical-align: top;
}

.ma-loading .ma-title span {
  font-size: 20px;
  display: inline-block;
  padding: 0 3px;
  animation: stretch 1s infinite;
}

.ma-loading .ma-title span:nth-child(1) {
  animation-delay: calc(1s / 8 * 0 / 2);
}

.ma-loading .ma-title span:nth-child(2) {
  animation-delay: calc(1s / 8 * 1 / 2);
}

.ma-loading .ma-title span:nth-child(3) {
  animation-delay: calc(1s / 8 * 2 / 2);
}

.ma-loading .ma-title span:nth-child(4) {
  animation-delay: calc(1s / 8 * 3 / 2);
}

.ma-loading .ma-title span:nth-child(5) {
  animation-delay: calc(1s / 8 * 4 / 2);
}

.ma-loading .ma-title span:nth-child(6) {
  animation-delay: calc(1s / 8 * 5 / 2);
}

.ma-loading .ma-title span:nth-child(7) {
  animation-delay: calc(1s / 8 * 6 / 2);
}

.ma-loading .ma-title span:nth-child(8) {
  animation-delay: calc(1s / 8 * 7 / 2);
}

.ma-loading .ma-loading-text {
  text-align: center;
  font-weight: bolder;
  font-style: italic;
  color: #889aa4;
  font-size: 14px;
  margin-top: 5px;
  animation: blink-loading 2s ease-in infinite;
}

@keyframes stretch {
  0% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.2);
  }
  50% {
    transform: scale(1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes blink-loading {
  0% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
  100% {
    opacity: 1;
  }
}
</style>