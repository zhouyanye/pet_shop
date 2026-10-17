<script>
export default{
  name:"AppFooter"
}
</script>
<script setup>
const services = [
  { icon: 'fa-solid fa-truck-fast', title: '满99包邮', desc: '全国大部分地区' },
  { icon: 'fa-solid fa-rotate-left', title: '7天无理由', desc: '退换无忧' },
  { icon: 'fa-solid fa-certificate', title: '正品保证', desc: '假一赔十' },
  { icon: 'fa-solid fa-robot', title: 'AI 客服', desc: '7x24小时在线' },
]

const links = [
  { title: '购物指南', items: ['购物流程', '会员介绍', '常见问题', '联系客服'] },
  { title: '配送服务', items: ['配送方式', '配送范围', '配送时间', '运费说明'] },
  { title: '支付方式', items: ['在线支付', '货到付款', '分期付款', '发票说明'] },
  { title: '售后服务', items: ['退换货政策', '退款说明', '返修政策', '价格保护'] },
]
</script>

<template>
  <footer class="app-footer">
    <!-- 服务保障 -->
    <div class="footer-services">
      <div class="footer-services__inner">
        <div v-for="s in services" :key="s.title" class="service-item">
          <span class="service-item__icon"><i :class="s.icon"></i></span>
          <div>
            <div class="service-item__title">{{ s.title }}</div>
            <div class="service-item__desc">{{ s.desc }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 链接区 -->
    <div class="footer-links">
      <div class="footer-links__inner">
        <div v-for="col in links" :key="col.title" class="link-col">
          <h4>{{ col.title }}</h4>
          <ul>
            <li v-for="item in col.items" :key="item">
              <a>{{ item }}</a>
            </li>
          </ul>
        </div>
        <div class="link-col link-col--qr">
          <h4>关注我们</h4>
          <div class="qr-placeholder">小程序码</div>
          <p>扫码进入小程序</p>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="footer-bottom">
      <p>© 2026 萌宠优选 PetShop</p>
      <p>让每一只毛孩子都过上幸福生活 <i class="fa-solid fa-paw"></i></p>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: #1A2E25;
  color: #C8D8D2;
  margin-top: 40px;
}

.footer-services {
  background: #FAFAFA;
  border-top: 1px solid #F0F0F0;
}
.footer-services__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
  display: flex;
  justify-content: space-around;
  gap: 16px;
}
.service-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.service-item__icon {
  font-size: 24px;
  color: #3DA67E;
}
.service-item__title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.service-item__desc {
  font-size: 13px;
  color: #999;
}

.footer-links__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr) 200px;
  gap: 24px;
}
.link-col h4 {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 16px;
}
.link-col ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.link-col li {
  margin-bottom: 10px;
}
.link-col a {
  font-size: 13px;
  color: #8DA8A0;
  cursor: pointer;
  transition: color 0.2s;
}
.link-col a:hover {
  color: #5BC098;
}
.link-col--qr {
  text-align: center;
}
.qr-placeholder {
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #8DA8A0;
  margin: 0 auto 8px;
}
.link-col--qr p {
  font-size: 12px;
  color: #8DA8A0;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px 16px;
  text-align: center;
  font-size: 13px;
  color: #8DA8A0;
}
.footer-bottom p {
  margin: 4px 0;
}

@media (max-width: 768px) {
  .footer-services__inner {
    flex-wrap: wrap;
    justify-content: center;
    gap: 20px;
  }
  .footer-links__inner {
    grid-template-columns: repeat(2, 1fr);
  }
  .link-col--qr {
    grid-column: span 2;
  }
}
</style>
