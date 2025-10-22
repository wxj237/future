import request from '@/utils/request'

// 收件箱相关API
export function getInboxList(params) {
  return request({
    url: '/sys/inbox/list',
    method: 'get',
    params
  })
}

export function markInboxAsRead(id) {
  return request({
    url: `/sys/inbox/markAsRead/${id}`,
    method: 'post'
  })
}

export function getUnreadCount(userId) {
  return request({
    url: '/sys/inbox/unreadCount',
    method: 'get',
    params: { userId }
  })
}

// 提醒相关API
export function getRemindList(userId) {
  return request({
    url: '/sys/inboxRemind/userReminds',
    method: 'get',
    params: { userId }
  })
}

export function getUnreadRemindCount(userId) {
  return request({
    url: '/sys/inboxRemind/unreadCount',
    method: 'get',
    params: { userId }
  })
}

export function markRemindAsRead(id) {
  return request({
    url: `/sys/inboxRemind/markAsRead/${id}`,
    method: 'post'
  })
}

export function markAllRemindsAsRead(userId) {
  return request({
    url: '/sys/inboxRemind/markAllAsRead',
    method: 'post',
    params: { userId }
  })
}

// 发送通知API
export function sendToGroup(data) {
  return request({
    url: '/sys/inbox/sendToGroup',
    method: 'post',
    data
  })
}

export function sendToUsers(data) {
  return request({
    url: '/sys/inbox/sendToUsers',
    method: 'post',
    data
  })
}

export function sendToAll(data) {
  return request({
    url: '/sys/inbox/sendToAll',
    method: 'post',
    data
  })
}
