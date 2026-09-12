# -*- coding: utf-8 -*-
import io

p = r'D:\codex\Ai_emo\ai-vue\src\views\consultations.vue'
s = io.open(p, encoding='utf-8').read()
old = '<el-table-column label="会话ID" width="100">'
new = '<el-table-column label="用户" width="100">'
assert old in s, 'old not found'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('OK consultations.vue')
