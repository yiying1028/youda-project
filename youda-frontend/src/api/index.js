import request from '@/utils/request.js'

const normalizeChatId = (value, seen = new WeakSet()) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (typeof value !== 'object') return ''
  if (seen.has(value)) return ''
  seen.add(value)
  if (typeof value.chatId === 'string') return value.chatId
  if (typeof value.id === 'string' && value.id.startsWith('chat_')) return value.id
  if (value.data) {
    const nestedChatId = normalizeChatId(value.data, seen)
    if (nestedChatId) return nestedChatId
  }
  for (const nested of Object.values(value)) {
    const nestedChatId = normalizeChatId(nested, seen)
    if (nestedChatId) return nestedChatId
  }
  return ''
}

const resolveChatId = (value) => {
  const chatId = normalizeChatId(value)
  if (!chatId) {
    throw new Error('Invalid chatId')
  }
  return encodeURIComponent(chatId)
}

const normalizeAuthor = (author = {}, fallback = {}) => ({
  authorId: fallback.authorId ?? author.userId ?? author.id ?? null,
  authorName: fallback.authorName ?? author.nickname ?? author.name ?? '',
  authorAvatar: fallback.authorAvatar ?? author.avatar ?? ''
})

const normalizePost = (post = {}) => {
  const author = normalizeAuthor(post.author, post)
  const id = post.id ?? post.postId ?? null

  return {
    ...post,
    ...author,
    id,
    postId: post.postId ?? id,
    summary: post.summary ?? post.content ?? '',
    viewCount: post.viewCount ?? 0,
    likeCount: post.likeCount ?? 0,
    commentCount: post.commentCount ?? 0,
    collectCount: post.collectCount ?? 0,
    isTop: Boolean(post.isTop),
    isLiked: Boolean(post.isLiked),
    isCollected: Boolean(post.isCollected)
  }
}

const normalizeComment = (comment = {}) => {
  const author = normalizeAuthor(comment.author, comment)
  const id = comment.id ?? comment.commentId ?? null

  return {
    ...comment,
    ...author,
    id,
    commentId: comment.commentId ?? id
  }
}

const normalizeFavorite = (item = {}) => ({
  ...normalizePost(item),
  title: item.title ?? item.postTitle ?? '',
  postTitle: item.postTitle ?? item.title ?? ''
})

const normalizeVideo = (video = {}) => ({
  ...video,
  id: video.id ?? video.videoId ?? null,
  videoId: video.videoId ?? video.id ?? null,
  duration: video.duration ?? 0,
  isFinished: Boolean(video.isFinished)
})

const normalizeChapter = (chapter = {}) => ({
  ...chapter,
  id: chapter.id ?? chapter.chapterId ?? null,
  chapterId: chapter.chapterId ?? chapter.id ?? null,
  videos: Array.isArray(chapter.videos) ? chapter.videos.map(normalizeVideo) : []
})

const normalizeCourse = (course = {}) => {
  const chapters = Array.isArray(course.chapters) ? course.chapters.map(normalizeChapter) : []
  const derivedVideoCount = chapters.reduce((total, chapter) => total + (chapter.videos?.length || 0), 0)

  return {
    ...course,
    id: course.id ?? course.courseId ?? null,
    courseId: course.courseId ?? course.id ?? null,
    cover: course.cover ?? course.coverImage ?? '',
    coverImage: course.coverImage ?? course.cover ?? '',
    learnCount: course.learnCount ?? course.studentCount ?? 0,
    studentCount: course.studentCount ?? course.learnCount ?? 0,
    chapterCount: course.chapterCount ?? chapters.length,
    videoCount: course.videoCount ?? (derivedVideoCount > 0 ? derivedVideoCount : (course.chapterCount ?? 0)),
    chapters
  }
}

const normalizeVideoPlay = (video = {}) => ({
  ...video,
  id: video.id ?? video.videoId ?? null,
  videoId: video.videoId ?? video.id ?? null,
  url: video.url ?? video.videoUrl ?? '',
  videoUrl: video.videoUrl ?? video.url ?? '',
  progress: video.progress ?? video.lastPosition ?? 0,
  lastPosition: video.lastPosition ?? video.progress ?? 0,
  prevVideoId: video.prevVideoId ?? null,
  nextVideoId: video.nextVideoId ?? null
})

const normalizePage = (result, itemMapper) => {
  if (!result) return result
  if (Array.isArray(result)) {
    return result.map(itemMapper)
  }
  if (Array.isArray(result.records)) {
    return {
      ...result,
      records: result.records.map(itemMapper)
    }
  }
  return itemMapper(result)
}

export const userRegister = (data) => request.post('/user/register', data)
export const userLogin = (data) => request.post('/user/login', data)
export const getUserInfo = () => request.get('/user/info')
export const updateUserInfo = (data) => request.put('/user/info', data)
export const updatePassword = (data) => request.put('/user/password', data)
export const uploadAvatar = (formData) =>
  request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const getMyPosts = async (params) => normalizePage(await request.get('/user/posts', { params }), normalizePost)
export const getMyFavorites = async (params) =>
  normalizePage(await request.get('/user/favorites', { params }), normalizeFavorite)

export const getCategoryList = () => request.get('/category/list')
export const getSubjectList = () => request.get('/subject/list')
export const getGradeList = () => request.get('/grade/list')
export const getAnnouncementList = (params) => request.get('/announcement/list', { params })

export const commonUpload = (formData) =>
  request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const getPostList = async (params) => normalizePage(await request.get('/post/list', { params }), normalizePost)
export const getPostDetail = async (id) => normalizePost(await request.get(`/post/${id}`))
export const createPost = async (data) => normalizePost(await request.post('/post', data))
export const deletePost = (id) => request.delete(`/post/${id}`)
export const togglePostLike = (id) => request.post(`/post/${id}/like`)
export const togglePostCollect = (id) => request.post(`/post/${id}/collect`)
export const getPostComments = async (id, params) =>
  normalizePage(await request.get(`/post/${id}/comments`, { params }), normalizeComment)
export const addComment = (id, data) => request.post(`/post/${id}/comment`, data)
export const deleteComment = (id) => request.delete(`/comment/${id}`)

export const getResourceList = (params) => request.get('/resource/list', { params })
export const getResourceDetail = (id) => request.get(`/resource/${id}`)
export const downloadResource = (id) => {
  window.open(`/api/resource/${id}/download`, '_blank')
}
export const uploadResource = (formData) =>
  request.post('/resource/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const createChatSession = () => request.post('/chat/create')
export const getChatList = (params) => request.get('/chat/list', { params })
export const getChatDetail = (chatId) => request.get(`/chat/${resolveChatId(chatId)}`)
export const sendMessage = (chatId, data) => request.post(`/chat/${resolveChatId(chatId)}/message`, data)
export const deleteChat = (chatId) => request.delete(`/chat/${resolveChatId(chatId)}`)
export const uploadChatImage = (formData) =>
  request.post('/chat/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const getCourseList = async (params) => normalizePage(await request.get('/course/list', { params }), normalizeCourse)
export const getCourseDetail = async (id) => normalizeCourse(await request.get(`/course/${id}`))
export const getVideoInfo = async (id) => normalizeVideoPlay(await request.get(`/course/video/${id}`))
export const updateVideoProgress = (id, data) => request.post(`/course/video/${id}/progress`, data)
export const getLearningRecords = () => request.get('/course/learning-records')

export const getNotebookList = (params) => request.get('/notebook/list', { params })
export const createNotebookItem = (data) => request.post('/notebook', data)
export const updateNotebookItem = (id, data) => request.put(`/notebook/${id}`, data)
export const deleteNotebookItem = (id) => request.delete(`/notebook/${id}`)
export const getNotebookStats = () => request.get('/notebook/stats')
export const getNotebookReviewRandom = (params) => request.get('/notebook/review/random', { params })
export const updateNotebookMasteryStatus = (id, status) =>
  request.put(`/notebook/${id}/mastery-status`, null, { params: { status } })

export const getPointsOverview = () => request.get('/points/overview')
export const dailyCheckIn = () => request.post('/points/checkin')
export const getPointsRanking = () => request.get('/points/ranking')
export const getPointsRecords = (params) => request.get('/points/records', { params })

export const getAdminStatistics = () => request.get('/admin/statistics')
export const getAdminUserList = (params) => request.get('/admin/user/list', { params })
export const updateUserStatus = (id, status) =>
  request.put(`/admin/user/${id}/status`, null, { params: { status } })
export const getAdminPostList = (params) => request.get('/admin/post/list', { params })
export const adminDeletePost = (id) => request.delete(`/admin/post/${id}`)
export const setPostTop = (id, isTop) =>
  request.put(`/admin/post/${id}/top`, null, { params: { isTop } })
export const getAdminResourceList = (params) => request.get('/admin/resource/list', { params })
export const adminDeleteResource = (id) => request.delete(`/admin/resource/${id}`)
export const getAdminCourseList = (params) => request.get('/admin/course/list', { params })
export const adminAddCourse = (data) => request.post('/admin/course', data)
export const adminUpdateCourse = (id, data) => request.put(`/admin/course/${id}`, data)
export const adminDeleteCourse = (id) => request.delete(`/admin/course/${id}`)
export const adminAddChapter = (courseId, data) => request.post(`/admin/course/${courseId}/chapter`, data)
export const adminUploadVideo = (chapterId, formData) =>
  request.post(`/admin/chapter/${chapterId}/video`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const getAdminAnnouncementList = (params) => request.get('/admin/announcement/list', { params })
export const adminAddAnnouncement = (data) => request.post('/admin/announcement', data)
export const adminUpdateAnnouncement = (id, data) => request.put(`/admin/announcement/${id}`, data)
export const adminDeleteAnnouncement = (id) => request.delete(`/admin/announcement/${id}`)
