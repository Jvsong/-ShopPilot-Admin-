<template>
  <div class="user-edit-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="top"
    >
      <div class="form-grid">
        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>

          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              id="username"
              placeholder="请输入用户名"
              maxlength="50"
              show-word-limit
              :disabled="!!props.userId"
            />
          </el-form-item>

          <el-form-item :label="props.userId ? '重置密码' : '密码'" prop="password" v-if="!props.userId">
            <el-input
              v-model="form.password"
              id="password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword" v-if="!props.userId">
            <el-input
              v-model="form.confirmPassword"
              id="confirm-password"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              id="email"
              placeholder="请输入邮箱"
              type="email"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              id="phone"
              placeholder="请输入手机号"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item label="用户昵称" prop="nickname">
            <el-input
              v-model="form.nickname"
              id="nickname"
              placeholder="请输入用户昵称"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 账户设置 -->
        <div class="form-section">
          <h3 class="section-title">账户设置</h3>

          <el-form-item label="用户头像">
            <el-upload
              class="avatar-upload"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <img v-if="form.avatar" :src="form.avatar" class="avatar-image" />
              <div v-else class="avatar-placeholder">
                <el-icon><UserFilled /></el-icon>
                <div>点击上传头像</div>
              </div>
            </el-upload>
            <div class="upload-tips">
              建议尺寸：200×200px，支持 JPG、PNG 格式，大小不超过 1MB
            </div>
          </el-form-item>

          <el-form-item label="用户类型" prop="userType">
            <el-select
              v-model="form.userType"
              id="user-type"
              placeholder="请选择用户类型"
              style="width: 100%;"
            >
              <el-option label="普通用户" :value="1" />
              <el-option label="管理员" :value="2" />
              <el-option label="商家" :value="3" />
            </el-select>
          </el-form-item>

          <el-form-item label="账户状态" prop="status">
            <el-select
              v-model="form.status"
              id="user-status"
              placeholder="请选择账户状态"
              style="width: 100%;"
            >
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>

          <el-form-item label="登录失败次数" prop="loginFailCount" v-if="props.userId">
            <el-input-number
              v-model="form.loginFailCount"
              id="login-fail-count"
              :min="0"
              :max="10"
              placeholder="登录失败次数"
              style="width: 100%;"
            />
          </el-form-item>
        </div>

        <!-- 角色分配 -->
        <div class="form-section full-width" v-if="roleList.length > 0">
          <h3 class="section-title">角色分配</h3>
          <div class="role-assignment">
            <el-checkbox-group
              v-model="form.roleIds"
              id="role-checkbox-group"
              class="role-checkboxes"
            >
              <el-checkbox
                v-for="role in roleList"
                :key="role.id"
                :label="role.id"
                :value="role.id"
                class="role-checkbox"
              >
                <div class="role-item">
                  <span class="role-name">{{ role.name }}</span>
                  <span class="role-code">{{ role.code }}</span>
                  <el-tag
                    v-if="role.status === 0"
                    size="small"
                    type="danger"
                    class="role-status-tag"
                  >
                    已禁用
                  </el-tag>
                </div>
              </el-checkbox>
            </el-checkbox-group>
            <div class="role-tips" v-if="roleList.length === 0">
              暂无可用角色，请先创建角色
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button
          type="primary"
          id="save-user-btn"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '保存中...' : '保存用户' }}
        </el-button>
        <el-button
          id="cancel-btn"
          @click="handleCancel"
        >
          取消
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type UploadProps } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { http } from '@/utils/request'

const props = defineProps<{
  userId?: number | null
}>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const roleList = ref<any[]>([])

interface UserDetailResponse {
  id?: number
  roleIds?: number[]
  roles?: Array<{ id: number }>
  [key: string]: any
}

interface PageListResponse<T> {
  list?: T[]
  total?: number
  [key: string]: any
}

// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '',
  avatar: '',
  userType: 1,
  status: 1,
  loginFailCount: 0,
  roleIds: [] as number[]
})

// 验证规则
const validateUsername = (rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线，长度3-20位'))
  } else {
    callback()
  }
}

const validatePassword = (rule: any, value: string, callback: Function) => {
  if (!props.userId) {
    if (!value) {
      callback(new Error('请输入密码'))
    } else if (value.length < 6) {
      callback(new Error('密码长度不能少于6位'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: string, callback: Function) => {
  if (!props.userId) {
    if (!value) {
      callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const validateEmail = (rule: any, value: string, callback: Function) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validatePhone = (rule: any, value: string, callback: Function) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateNickname = (rule: any, value: string, callback: Function) => {
  if (value && value.length > 50) {
    callback(new Error('昵称长度不能超过50个字符'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  nickname: [
    { validator: validateNickname, trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择账户状态', trigger: 'change' }
  ],
  loginFailCount: [
    { required: false }
  ]
}

// 头像上传处理
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    form.avatar = response.data.url
    ElMessage.success('头像上传成功')
  }
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt1M) {
    ElMessage.error('图片大小不能超过 1MB')
    return false
  }
  return true
}

// 加载用户数据
const loadUser = async () => {
  if (!props.userId) return

  try {
    const response = await http.get<UserDetailResponse>(`/admin/users/${props.userId}`)
    Object.assign(form, response)
    // 清除密码字段
    form.password = ''
    form.confirmPassword = ''
    // 加载用户角色
    if (response.roleIds) {
      form.roleIds = response.roleIds
    } else {
      // 如果没有roleIds字段，尝试从roles字段获取
      if (response.roles && Array.isArray(response.roles)) {
        form.roleIds = response.roles.map((role: any) => role.id)
      }
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    ElMessage.error('加载用户数据失败')
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await http.get<PageListResponse<any>>('/admin/roles', { status: 1 })
    roleList.value = Array.isArray(response.list) ? response.list : []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const { password, confirmPassword, ...baseData } = form
    const submitData = props.userId
      ? { ...baseData }
      : { ...baseData, password, confirmPassword }
    // 编辑时不传递密码相关字段
    if (props.userId) {
      // no-op: password fields are excluded for updates above
    }

    // 处理角色数据
    if (submitData.roleIds && Array.isArray(submitData.roleIds)) {
      submitData.roleIds = submitData.roleIds.filter(id => id)
    }

    if (props.userId) {
      // 更新用户
      await http.put(`/admin/users/${props.userId}`, submitData)
      ElMessage.success('用户更新成功')
    } else {
      // 创建用户
      await http.post<number>('/admin/users', submitData)
      ElMessage.success('用户创建成功')
    }

    emit('success')
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error('保存用户失败')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 监听userId变化
watch(() => props.userId, (newVal) => {
  if (newVal) {
    loadUser()
  } else {
    // 重置表单
    Object.assign(form, {
      username: '',
      password: '',
      confirmPassword: '',
      email: '',
      phone: '',
      nickname: '',
      avatar: '',
      userType: 1,
      status: 1,
      loginFailCount: 0,
      roleIds: []
    })
  }
}, { immediate: true })

// 加载角色列表
onMounted(() => {
  loadRoles()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.user-edit-form {
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-xl;
    margin-bottom: $spacing-xl;

    @media (max-width: 992px) {
      grid-template-columns: 1fr;
      gap: $spacing-lg;
    }

    .full-width {
      grid-column: 1 / -1;
    }
  }

  .form-section {
    .section-title {
      font-size: $font-size-lg;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: $spacing-lg;
      padding-bottom: $spacing-sm;
      border-bottom: 1px solid $border-light;
    }

    .el-form-item {
      margin-bottom: $spacing-xl;
    }
  }

  .avatar-upload {
    width: 120px;
    height: 120px;
    border: 2px dashed $border-color;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: $primary-color;
    }

    .avatar-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: $text-secondary;
      padding: $spacing-lg;

      .el-icon {
        font-size: $font-size-xxl;
        margin-bottom: $spacing-md;
      }
    }
  }

  .upload-tips {
    margin-top: $spacing-sm;
    color: $text-secondary;
    font-size: $font-size-sm;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: $spacing-lg;
    margin-top: $spacing-xl;
    padding-top: $spacing-lg;
    border-top: 1px solid $border-light;
  }

  .role-assignment {
    background: rgba($primary-color, 0.02);
    border: 1px solid $border-light;
    border-radius: $border-radius;
    padding: $spacing-lg;

    .role-checkboxes {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: $spacing-md;
      margin-top: $spacing-md;
    }

    .role-checkbox {
      width: 100%;

      :deep(.el-checkbox__label) {
        width: 100%;
      }
    }

    .role-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding: $spacing-sm;

      .role-name {
        font-weight: 500;
        color: $text-primary;
      }

      .role-code {
        color: $text-secondary;
        font-size: $font-size-sm;
        margin-left: $spacing-sm;
      }

      .role-status-tag {
        margin-left: $spacing-sm;
      }
    }

    .role-tips {
      text-align: center;
      color: $text-secondary;
      font-size: $font-size-sm;
      padding: $spacing-lg;
      background: $border-light;
      border-radius: $border-radius;
    }
  }
}
</style>
