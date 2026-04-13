<template>
  <div class="product-edit-form">
    <div v-if="message.text" :class="[message.type === 'success' ? 'success-message' : 'error-message']">
      {{ message.text }}
    </div>

    <form class="form-layout" @submit.prevent="handleSubmit">
      <div class="form-grid">
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>

          <label class="field-label" for="product-name">商品名称</label>
          <input id="product-name" v-model.trim="form.name" class="form-input" maxlength="100" placeholder="请输入商品名称" />

          <label class="field-label" for="product-category">商品分类</label>
          <select id="product-category" v-model="form.categoryId" class="form-select">
            <option value="">请选择商品分类</option>
            <option v-for="category in categoryList" :key="category.id" :value="String(category.id)">
              {{ category.name }}
            </option>
          </select>

          <label class="field-label" for="product-price">商品价格</label>
          <input id="product-price" v-model.number="form.price" class="form-input" type="number" min="0" step="0.01" placeholder="请输入商品价格" />

          <label class="field-label" for="product-stock">商品库存</label>
          <input id="product-stock" v-model.number="form.stock" class="form-input" type="number" min="0" placeholder="请输入商品库存" />
        </div>

        <div class="form-section">
          <h3 class="section-title">状态与展示</h3>

          <label class="field-label" for="product-status">商品状态</label>
          <select id="product-status" v-model="form.status" class="form-select">
            <option value="1">上架</option>
            <option value="0">下架</option>
          </select>

          <label class="field-label" for="product-images">商品主图</label>
          <input id="product-images" class="form-input" type="file" accept="image/*" @change="handleImageChange" />
          <div class="upload-tips">支持 JPG、PNG，当前为演示上传，不会实际发到服务器。</div>

          <div v-if="form.mainImage" class="image-preview">
            <img :src="form.mainImage" class="product-image" />
          </div>

          <div class="checkbox-group">
            <label><input v-model="form.isHot" type="checkbox" /> 热销商品</label>
            <label><input v-model="form.isNew" type="checkbox" /> 新品推荐</label>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">商品描述</h3>
        <textarea
          id="product-description"
          v-model.trim="form.description"
          class="form-textarea"
          rows="6"
          maxlength="2000"
          placeholder="请输入商品详细描述"
        />
      </div>

      <div class="form-actions">
        <button id="save-btn" type="submit" class="primary-button" :disabled="submitting">
          {{ submitting ? '保存中...' : '保存商品' }}
        </button>
        <button id="cancel-btn" type="button" class="secondary-button" @click="handleCancel">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '@/utils/request'

const props = defineProps<{
  productId?: number | null
}>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const submitting = ref(false)
const message = reactive({
  type: 'success' as 'success' | 'error',
  text: ''
})

const categoryList = ref([
  { id: 1, name: '电子产品' },
  { id: 2, name: '服装' },
  { id: 3, name: '家居生活' },
  { id: 4, name: '图书音像' },
  { id: 5, name: '美妆个护' }
])

const createDefaultForm = () => ({
  name: '',
  categoryId: '',
  price: 0,
  stock: 0,
  status: '1',
  description: '',
  mainImage: '',
  isHot: false,
  isNew: false
})

const form = reactive(createDefaultForm())

const setMessage = (type: 'success' | 'error', text: string) => {
  message.type = type
  message.text = text
}

const resetForm = () => {
  Object.assign(form, createDefaultForm())
  message.text = ''
}

const loadProduct = async () => {
  if (!props.productId) {
    resetForm()
    return
  }

  try {
    const response = await http.get(`/admin/products/${props.productId}`)
    Object.assign(form, {
      ...createDefaultForm(),
      ...response,
      categoryId: response.categoryId ? String(response.categoryId) : '',
      status: String(response.status ?? 1)
    })
  } catch (error) {
    console.error('加载商品数据失败:', error)
    setMessage('error', '加载商品数据失败')
    ElMessage.error('加载商品数据失败')
  }
}

const validateForm = () => {
  if (!form.name || form.name.length < 2 || form.name.length > 100) {
    setMessage('error', '商品名称长度在 2 到 100 个字符')
    return false
  }
  if (!form.categoryId) {
    setMessage('error', '请选择商品分类')
    return false
  }
  if (Number(form.price) < 0) {
    setMessage('error', '商品价格不能为负数')
    return false
  }
  if (Number(form.stock) < 0) {
    setMessage('error', '商品库存不能为负数')
    return false
  }
  return true
}

const buildPayload = () => ({
  name: form.name,
  categoryId: Number(form.categoryId),
  price: Number(form.price),
  stock: Number(form.stock),
  status: Number(form.status),
  description: form.description,
  mainImage: form.mainImage,
  isHot: form.isHot ? 1 : 0,
  isNew: form.isNew ? 1 : 0
})

const handleImageChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = () => {
    form.mainImage = String(reader.result || '')
  }
  reader.readAsDataURL(file)
}

const handleSubmit = async () => {
  if (!validateForm()) return

  try {
    submitting.value = true
    if (props.productId) {
      await http.put(`/admin/products/${props.productId}`, buildPayload())
      setMessage('success', '商品更新成功')
      ElMessage.success('商品更新成功')
    } else {
      await http.post('/admin/products', buildPayload())
      setMessage('success', '商品创建成功')
      ElMessage.success('商品创建成功')
    }
    emit('success')
  } catch (error) {
    console.error('保存商品失败:', error)
    setMessage('error', '保存商品失败')
    ElMessage.error('保存商品失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

watch(
  () => props.productId,
  () => {
    loadProduct()
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.product-edit-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.form-layout {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-xl;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.field-label {
  color: $text-secondary;
  font-size: $font-size-sm;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  min-height: 40px;
  padding: 10px 12px;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  background: #fff;
}

.form-textarea {
  min-height: 120px;
  resize: vertical;
}

.upload-tips {
  color: $text-secondary;
  font-size: $font-size-sm;
}

.image-preview {
  margin-top: $spacing-sm;
}

.product-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: $border-radius;
  border: 1px solid $border-light;
}

.checkbox-group {
  display: flex;
  gap: $spacing-lg;
  margin-top: $spacing-sm;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: $spacing-lg;
}

.primary-button,
.secondary-button {
  min-height: 40px;
  padding: 0 18px;
  border-radius: $border-radius;
  cursor: pointer;
  border: 1px solid transparent;
}

.primary-button {
  background: $primary-color;
  color: $text-white;
}

.secondary-button {
  background: #fff;
  color: $text-primary;
  border-color: $border-color;
}

.success-message,
.error-message {
  padding: $spacing-md;
  border-radius: $border-radius;
}

.success-message {
  color: $success-color;
  background: rgba($success-color, 0.1);
}

.error-message {
  color: $danger-color;
  background: rgba($danger-color, 0.1);
}

@media (max-width: 992px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
