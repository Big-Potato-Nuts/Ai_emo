# -*- coding: utf-8 -*-
import io

def patch(path, old, new, count=1):
    s = io.open(path, encoding='utf-8').read()
    assert old in s, 'NOT FOUND in %s: %s' % (path, old[:60])
    s = s.replace(old, new, count)
    io.open(path, 'w', encoding='utf-8', newline='').write(s)
    print('OK:', path)

# 1. frontendKnowledge.vue 标题 + 图片域名
patch(r'D:\codex\Ai_emo\ai-vue\src\views\frontendKnowledge.vue',
      '<h1>情绪日志</h1>', '<h1>心理健康知识库</h1>')
patch(r'D:\codex\Ai_emo\ai-vue\src\views\frontendKnowledge.vue',
      "    import iconUrl from '@/assets/images/book.png'\n    import { Platform } from '@element-plus/icons-vue'",
      "    import iconUrl from '@/assets/images/book.png'\n    import { Platform } from '@element-plus/icons-vue'\n    import { fileBaseUrl } from '@/config/index.js'")
patch(r'D:\codex\Ai_emo\ai-vue\src\views\frontendKnowledge.vue',
      "return url ? 'http://159.75.169.224:1235' + url : 'https://file.itndedu.com/psychology_ai.png'",
      "return url ? fileBaseUrl + url : 'https://file.itndedu.com/psychology_ai.png'")

# 2. Navbar.vue 用户名
patch(r'D:\codex\Ai_emo\ai-vue\src\components\Navbar.vue',
      '<p class="user-name">admin</p>',
      '<p class="user-name">{{ userInfo.nickname || userInfo.username || \'admin\' }}</p>')
patch(r'D:\codex\Ai_emo\ai-vue\src\components\Navbar.vue',
      'const route = useRoute()',
      'const route = useRoute()\n\n// 读取当前登录用户信息\nlet userInfo = {}\ntry {\n    userInfo = JSON.parse(localStorage.getItem(\'userInfo\') || \'{}\')\n} catch (e) {\n    userInfo = {}\n}')

# 3. FrontendLayout.vue 导航栏用户昵称
patch(r'D:\codex\Ai_emo\ai-vue\src\components\FrontendLayout.vue',
      '<el-button v-if="isLoggedIn" class="logout-btn" @click="handleLogout">退出登录</el-button>',
      '<span v-if="isLoggedIn" class="nav-link user-nickname">{{ nickname }}</span>\n                <el-button v-if="isLoggedIn" class="logout-btn" @click="handleLogout">退出登录</el-button>')
patch(r'D:\codex\Ai_emo\ai-vue\src\components\FrontendLayout.vue',
      'const isLoggedIn = ref(false)',
      'const isLoggedIn = ref(false)\nconst nickname = ref(\'\')')
patch(r'D:\codex\Ai_emo\ai-vue\src\components\FrontendLayout.vue',
      'onMounted(() => {\n   isLoggedIn.value = localStorage.getItem(\'token\') !== null\n})',
      'onMounted(() => {\n   isLoggedIn.value = localStorage.getItem(\'token\') !== null\n   if (isLoggedIn.value) {\n       const userInfo = JSON.parse(localStorage.getItem(\'userInfo\') || \'{}\')\n       nickname.value = userInfo.nickname || userInfo.username || \'\'\n   }\n})')

# 4. router/index.js userInfo 防御
patch(r'D:\codex\Ai_emo\ai-vue\src\router\index.js',
      """    if (token) {
        const userInfo = JSON.parse(localStorage.getItem('userInfo'))
        // 如果是后台用户
        if (userInfo.userType == 2) {""",
      """    if (token) {
        let userInfo = null
        try {
            userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
        } catch (e) {
            userInfo = null
        }
        // token 存在但用户信息缺失时，按未登录处理
        if (!userInfo) {
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            return next('/auth/login')
        }
        // 如果是后台用户
        if (userInfo.userType == 2) {""")

# 5. index.html 标题
patch(r'D:\codex\Ai_emo\ai-vue\index.html',
      '<html lang="en">', '<html lang="zh-CN">')
patch(r'D:\codex\Ai_emo\ai-vue\index.html',
      '<title>ai-vue</title>', '<title>心理健康AI助手</title>')

# 6. dashboard.vue class=number -> value
patch(r'D:\codex\Ai_emo\ai-vue\src\views\dashboard.vue',
      '<p class="number">', '<p class="value">', 4)

print('ALL DONE')
