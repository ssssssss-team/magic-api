<template>
  <div
      v-if="visible"
      ref="menu"
      :class="[commonClass.menu, 'magic-contextmenu', customClass]"
      :style="{left: style.left + 'px', top: style.top + 'px', minWidth: style.minWidth + 'px', zIndex: style.zIndex}"
      @contextmenu="(e)=>e.preventDefault()"
  >
    <div class="magic-contextmenu-body">
      <template v-for="(item,index) of menus">
        <template v-if="!item.hidden">
          <div
              v-if="item.disabled"
              :key="index"
              :class="[
                commonClass.menuItem, commonClass.unclickableMenuItem,
                'magic-contextmenu-item', 'magic-contextmenu-item-disabled',
                item.divided?'magic-contextmenu-divided':null
              ]"
          >
            <div v-if="hasIcon" class="magic-contextmenu-item-icon">
              <i v-if="item.icon" :class="'ma-icon ' + item.icon"></i>
            </div>
            <span class="magic-contextmenu-item-label">{{ item.label }}</span>
            <div class="magic-contextmenu-item-expand-icon"></div>
          </div>
          <div
              v-else-if="item.children"
              :key="index"
              :class="[
                commonClass.menuItem, commonClass.unclickableMenuItem,
                'magic-contextmenu-item', 'magic-contextmenu-item-available',
                activeSubmenu.index===index? 'magic-contextmenu-item-expand':null,
                item.divided?'magic-contextmenu-item-divided':null
              ]"
              @mouseenter="($event)=>enterItem($event,item,index)"
          >
            <div v-if="hasIcon" class="magic-contextmenu-item-icon">
              <i v-if="item.icon" :class="'ma-icon ' + item.icon"></i>
            </div>
            <span class="magic-contextmenu-item-label">{{ item.label }}</span>
            <div class="magic-contextmenu-item-expand-icon">▶</div>
          </div>
          <div
              v-else
              :key="index"
              :class="[
                commonClass.menuItem, commonClass.clickableMenuItem,
                'magic-contextmenu-item', 'magic-contextmenu-item-available',
                item.divided?'magic-contextmenu-item-divided':null
              ]"
              @click="itemClick(item)"
              @mouseenter="($event)=>enterItem($event,item,index)"
          >
            <div v-if="hasIcon" class="magic-contextmenu-item-icon">
              <i v-if="item.icon" :class="'ma-icon ' + item.icon"></i>
            </div>
            <span class="magic-contextmenu-item-label">{{ item.label }}</span>
            <div class="magic-contextmenu-item-expand-icon"></div>
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script>
import Vue from "vue";
import {
  COMPONENT_NAME,
  SUBMENU_OPEN_TREND_LEFT,
  SUBMENU_OPEN_TREND_RIGHT,
  SUBMENU_X_OFFSET,
  SUBMENU_Y_OFFSET
} from "./constant";

export default {
  name: COMPONENT_NAME,
  data() {
    return {
      commonClass: {
        menu: null,
        menuItem: null,
        clickableMenuItem: null,
        unclickableMenuItem: null
      },
      activeSubmenu: {
        index: null,
        instance: null
      },
      menus: [],
      position: {
        x: 0,
        y: 0,
        width: 0,
        height: 0
      },
      style: {
        left: 0,
        top: 0,
        zIndex: 2,
        minWidth: 150
      },
      customClass: null,
      visible: false,
      hasIcon: false,
      openTrend: SUBMENU_OPEN_TREND_RIGHT
    };
  },
  mounted() {
    this.visible = true;
    for (let item of this.menus) {
      if (item.icon) {
        this.hasIcon = true;
        break;
      }
    }
    this.$nextTick(() => {
      const windowWidth = document.documentElement.clientWidth;
      const windowHeight = document.documentElement.clientHeight;
      const menu = this.$refs.menu;
      const menuWidth = menu.offsetWidth;
      const menuHeight = menu.offsetHeight;

      (this.openTrend === SUBMENU_OPEN_TREND_LEFT
          ? this.leftOpen
          : this.rightOpen)(windowWidth, windowHeight, menuWidth);

      this.style.top = this.position.y;
      if (this.position.y + menuHeight > windowHeight) {
        if (this.position.height === 0) {
          this.style.top = this.position.y - menuHeight;
        } else {
          this.style.top = windowHeight - menuHeight;
        }
      }
    });
  },
  methods: {
    leftOpen(windowWidth, windowHeight, menuWidth) {
      this.style.left = this.position.x - menuWidth;
      this.openTrend = SUBMENU_OPEN_TREND_LEFT;
      if (this.style.left < 0) {
        this.openTrend = SUBMENU_OPEN_TREND_RIGHT;
        if (this.position.width === 0) {
          this.style.left = 0;
        } else {
          this.style.left = this.position.x + this.position.width;
        }
      }
    },
    rightOpen(windowWidth, windowHeight, menuWidth) {
      this.style.left = this.position.x + this.position.width;
      this.openTrend = SUBMENU_OPEN_TREND_RIGHT;
      if (this.style.left + menuWidth > windowWidth) {
        this.openTrend = SUBMENU_OPEN_TREND_LEFT;
        if (this.position.width === 0) {
          this.style.left = windowWidth - menuWidth;
        } else {
          this.style.left = this.position.x - menuWidth;
        }
      }
    },
    enterItem(e, item, index) {
      if (!this.visible) {
        return;
      }
      if (this.activeSubmenu.instance) {
        if (this.activeSubmenu.index === index) {
          return;
        } else {
          this.activeSubmenu.instance.close();
          this.activeSubmenu.instance = null;
          this.activeSubmenu.index = null;
        }
      }
      if (!item.children) {
        return;
      }
      const menuItemClientRect = e.target.getBoundingClientRect();
      const SubmenuConstructor = Vue.component(COMPONENT_NAME);
      this.activeSubmenu.index = index;
      this.activeSubmenu.instance = new SubmenuConstructor();
      this.activeSubmenu.instance.menus = item.children;
      this.activeSubmenu.instance.openTrend = this.openTrend;
      this.activeSubmenu.instance.commonClass = this.commonClass;
      this.activeSubmenu.instance.position = {
        x: menuItemClientRect.x + SUBMENU_X_OFFSET,
        y: menuItemClientRect.y + SUBMENU_Y_OFFSET,
        width: menuItemClientRect.width - 2 * SUBMENU_X_OFFSET,
        height: menuItemClientRect.width
      };
      this.activeSubmenu.instance.style.minWidth =
          typeof item.minWidth === "number" ? item.minWidth : this.style.minWidth;
      this.activeSubmenu.instance.style.zIndex = this.style.zIndex;
      this.activeSubmenu.instance.customClass =
          typeof item.customClass === "string"
              ? item.customClass
              : this.customClass;
      this.activeSubmenu.instance.$mount();
      document.getElementsByClassName('ma-container')[0].append(this.activeSubmenu.instance.$el);
    },
    itemClick(item) {
      if (!this.visible) {
        return;
      }
      if (
          item &&
          !item.disabled &&
          !item.hidden &&
          typeof item.onClick === "function"
      ) {
        return item.onClick();
      }
    },
    close() {
      this.visible = false;
      if (this.activeSubmenu.instance) {
        this.activeSubmenu.instance.close();
      }
      this.$nextTick(() => {
        this.$destroy();
      });
    }
  }
};
</script>

<style>
.magic-contextmenu {
  position: fixed;
  border: 1px solid var(--border-color);
  background: var(--background);
}

.magic-contextmenu-body {
  display: block;
}

.magic-contextmenu-item {
  transition: 0.2s;
  height: 24px;
  line-height: 24px;
  padding: 0 10px;
  display: flex;
}

.magic-contextmenu-item-divided {
  border-bottom: 1px solid var(--border-color);
}

.magic-contextmenu-item .magic-contextmenu-item-icon {
  margin-right: 5px;
  width: 13px;
}
.magic-contextmenu-item .magic-contextmenu-item-icon i{
    font-size: 12px;
    color: var(--icon-color);
}

.magic-contextmenu-item .magic-contextmenu-item-label {
  flex: 1;
}

.magic-contextmenu-item .magic-contextmenu-item-expand-icon {
  margin-left: 10px;
  font-size: 6px;
  width: 10px;
}

.magic-contextmenu-item-available {
  color: var(--color);
  cursor: pointer;
}

.magic-contextmenu-item-available:hover {
  background: var(--select-option-hover-background);
  color: var(--select-option-hover-color);
}

.magic-contextmenu-item-disabled {
  color: var(--select-option-disabled-color);
  cursor: not-allowed;
}

.magic-contextmenu-item-expand {
  background: var(--select-option-hover-background);
  color: var(--select-option-hover-color);
}
</style>