import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import type { App } from 'vue'

// Element Plus主题配置 - 使用深蓝色主题
const elementPlusTheme = {
  colors: {
    primary: {
      DEFAULT: '#165DFF',
      'deep': '#0E42D2',
      'dark': '#072CA6'
    },
    success: '#00B42A',
    warning: '#FF7D00',
    danger: '#F53F3F',
    info: '#3491FA'
  }
}

export default {
  install(app: App) {
    // 注册Element Plus
    app.use(ElementPlus, {
      locale: zhCn,
      size: 'default'
    })

    // 注册所有图标
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
      app.component(key, component)
    }

    // 自定义主题样式注入
    const style = document.createElement('style')
    style.textContent = `
      :root {
        --el-color-primary: ${elementPlusTheme.colors.primary.DEFAULT};
        --el-color-primary-light-3: ${elementPlusTheme.colors.primary.DEFAULT}33;
        --el-color-primary-light-5: ${elementPlusTheme.colors.primary.DEFAULT}1a;
        --el-color-primary-light-7: ${elementPlusTheme.colors.primary.DEFAULT}0d;
        --el-color-primary-light-8: ${elementPlusTheme.colors.primary.DEFAULT}0a;
        --el-color-primary-light-9: ${elementPlusTheme.colors.primary.DEFAULT}05;
        --el-color-primary-dark-2: ${elementPlusTheme.colors.primary.deep};

        --el-color-success: ${elementPlusTheme.colors.success};
        --el-color-warning: ${elementPlusTheme.colors.warning};
        --el-color-danger: ${elementPlusTheme.colors.danger};
        --el-color-info: ${elementPlusTheme.colors.info};

        --el-border-radius-base: 8px;
        --el-border-radius-small: 4px;

        --el-box-shadow-light: 0 2px 8px rgba(0, 0, 0, 0.08);
        --el-box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .el-button--primary {
        background-color: var(--el-color-primary);
        border-color: var(--el-color-primary);
      }

      .el-button--primary:hover {
        background-color: var(--el-color-primary-dark-2);
        border-color: var(--el-color-primary-dark-2);
      }
    `
    document.head.appendChild(style)
  }
}