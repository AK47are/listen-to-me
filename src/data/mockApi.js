import mockData from '@/data/mockData'

// 模拟 API 服务
const mockApi = {
  // 用户相关
  auth: {
    login(data) {
      if (data.username === 'listener') {
        return Promise.resolve(mockData.listenerLogin)
      } else if (data.username === 'creator') {
        return Promise.resolve(mockData.creatorLogin)
      } else if (data.username === 'admin') {
        return Promise.resolve(mockData.adminLogin)
      } else {
        return Promise.resolve(mockData.login)
      }
    },
    register(data) {
      return Promise.resolve(mockData.register)
    },
    getImageCaptcha() {
      return Promise.resolve({ captchaUrl: 'https://picsum.photos/120/40' })
    },
    sendVerifyCode(data) {
      return Promise.resolve({})
    },
    refreshToken() {
      return Promise.resolve(mockData.login)
    }
  },
  user: {
    getProfile() {
      return Promise.resolve(mockData.profile)
    },
    updateProfile(data) {
      return Promise.resolve({})
    }
  },

  // 音频相关
  audio: {
    getStreamSign(audioId) {
      return Promise.resolve('https://example.com/audio.mp3')
    },
    getAudioDetail(id) {
      return Promise.resolve(mockData.audioDetail)
    },
    updateHistory(data) {
      return Promise.resolve({})
    },
    saveHistory(audioId) {
      return Promise.resolve({})
    },
    getHistoryPage(params) {
      return Promise.resolve(mockData.historyList)
    }
  },

  // 收藏相关
  favorite: {
    saveAudioAction(data) {
      return Promise.resolve({})
    },
    saveFavoriteFolder(data) {
      return Promise.resolve({})
    },
    getFolderList() {
      return Promise.resolve(mockData.folderList)
    },
    getFavoritePage(params) {
      return Promise.resolve(mockData.favoriteList)
    },
    deleteFavorite(data) {
      return Promise.resolve({})
    },
    deleteFavoriteFolder(folderId) {
      return Promise.resolve({})
    }
  },

  // 喜欢相关
  like: {
    saveAudioLike(data) {
      return Promise.resolve({})
    },
    getLikePage(params) {
      return Promise.resolve(mockData.likeList)
    }
  },

  // 评论相关
  comment: {
    saveComment(data) {
      return Promise.resolve({})
    },
    getCommentList(audioId, params) {
      return Promise.resolve(mockData.commentList)
    },
    deleteComment(commentId) {
      return Promise.resolve({})
    },
    saveCommentLike(data) {
      return Promise.resolve({})
    }
  },

  // 创作者相关
  creator: {
    uploadAudio(file) {
      return Promise.resolve('https://example.com/audio.mp3')
    },
    uploadCover(file) {
      return Promise.resolve('https://picsum.photos/400/400')
    },
    saveAudio(data) {
      return Promise.resolve({ audioId: 1, status: 'PENDING_TRANSCODE' })
    },
    getAudioPage(params) {
      return Promise.resolve(mockData.creatorAudioList)
    },
    getAudio(id) {
      return Promise.resolve(mockData.audioDetail)
    },
    getAudioStatus(id) {
      return Promise.resolve({ audioId: id, status: 'ONLINE' })
    },
    updateAudio(data) {
      return Promise.resolve({})
    },
    removeAudio(id) {
      return Promise.resolve({})
    },
    // 时间槽管理
    saveSlotBatch(slots) {
      return Promise.resolve({})
    },
    getSlotPage(params) {
      return Promise.resolve(mockData.slotList)
    },
    updateSlot(id, status) {
      return Promise.resolve({})
    },
    // 预约订单管理
    getConsultPage(params) {
      return Promise.resolve(mockData.consultOrderList)
    },
    confirmConsult(id, address) {
      return Promise.resolve({})
    },
    rejectConsult(id) {
      return Promise.resolve({})
    },
    completeConsult(id) {
      return Promise.resolve({})
    }
  },

  // 支付相关
  payment: {
    getBalance() {
      return Promise.resolve(mockData.balance)
    },
    recharge(data) {
      return Promise.resolve({ rechargeSn: 'RC202603280001', payUrl: 'https://example.com/pay' })
    },
    createOrder(audioId) {
      return Promise.resolve({ orderSn: 'ORD202603280001', status: 'SUCCESS', balanceAfter: 2801 })
    },
    getOrder(sn) {
      return Promise.resolve(mockData.orderList.records[0])
    },
    getOrderPage(params) {
      return Promise.resolve(mockData.orderList)
    }
  },

  // 推荐相关
  recommend: {
    getHotList() {
      return Promise.resolve(mockData.hotList)
    },
    getRecommendList(params) {
      return Promise.resolve(mockData.recommendList)
    }
  },

  // 用户预约相关
  userConsult: {
    // 获取创作者列表
    getCreatorList(params) {
      const { pageNum = 1, pageSize = 12, keyword = '' } = params
      let creators = mockData.creatorList || []
      
      if (keyword) {
        const lowerKeyword = keyword.toLowerCase()
        creators = creators.filter(c => 
          c.nickname.toLowerCase().includes(lowerKeyword) ||
          c.title.toLowerCase().includes(lowerKeyword) ||
          c.tags.some(tag => tag.toLowerCase().includes(lowerKeyword))
        )
      }
      
      const start = (pageNum - 1) * pageSize
      const end = start + pageSize
      return Promise.resolve({
        records: creators.slice(start, end),
        total: creators.length
      })
    },

    // 获取创作者详情
    getCreatorDetail(creatorId) {
      const creator = (mockData.creatorList || []).find(c => c.id === creatorId)
      return Promise.resolve(creator || mockData.creatorList[0])
    },

    // 获取创作者时间槽列表
    getCreatorSlots(params) {
      const { creatorId, pageNum = 1, pageSize = 10 } = params
      const allSlots = mockData.slotList.records.filter(slot => 
        slot.status === 'AVAILABLE'
      )
      const start = (pageNum - 1) * pageSize
      const end = start + pageSize
      return Promise.resolve({
        records: allSlots.slice(start, end),
        total: allSlots.length
      })
    },

    // 获取时间槽详情
    getSlotDetail(slotId) {
      const slot = mockData.slotList.records.find(s => s.id === slotId)
      return Promise.resolve(slot || mockData.slotList.records[0])
    },

    saveConsult(data) {
      return Promise.resolve(mockData.userConsultOrderList.records[0])
    },
    getMyConsultPage(params) {
      return Promise.resolve(mockData.userConsultOrderList)
    },
    cancelConsult(id) {
      return Promise.resolve({})
    },
    applyRefund(id, reason) {
      return Promise.resolve({})
    },
    // 根据时间范围查询可用时间槽
    getAvailableSlots(params) {
      const { startTime, endTime, pageNum = 1, pageSize = 10 } = params
      const allSlots = mockData.slotList.records.filter(slot => 
        slot.status === 'AVAILABLE' &&
        (!startTime || slot.startTime >= startTime) &&
        (!endTime || slot.endTime <= endTime)
      )
      // 添加创作者信息到时间槽
      const slotsWithCreator = allSlots.map(slot => ({
        ...slot,
        creatorId: 88,
        creatorName: '后端架构师',
        creatorAvatar: 'https://randomuser.me/api/portraits/men/45.jpg',
        creatorTitle: '资深后端工程师',
        creatorBio: '10年后端开发经验，擅长微服务架构设计'
      }))
      const start = (pageNum - 1) * pageSize
      const end = start + pageSize
      return Promise.resolve({
        records: slotsWithCreator.slice(start, end),
        total: slotsWithCreator.length
      })
    }
  },

  // 管理员相关
  admin: {
    // 管理员登录
    login(data) {
      if (data.username === 'admin') {
        return Promise.resolve(mockData.adminLogin)
      } else {
        return Promise.resolve(mockData.login)
      }
    },
    
    // 待审音频分页
    getAuditAudioPage(params) {
      return Promise.resolve(mockData.auditAudioList)
    },
    
    // 执行音频审核
    auditAudio(data) {
      return Promise.resolve({})
    },
    
    // 封禁/解封用户
    updateUserStatus(data) {
      return Promise.resolve({})
    },
    
    // 待审申请分页
    getAuditApplyPage(params) {
      return Promise.resolve(mockData.auditApplyList)
    },
    
    // 执行申请审核
    auditApply(data) {
      return Promise.resolve({})
    },
    
    // 全站数据大盘
    getDashboard(params) {
      return Promise.resolve(mockData.dashboard)
    },
    
    // 退款申请分页
    getRefundApplyPage(params) {
      return Promise.resolve(mockData.refundApplyList)
    },
    
    // 审核退款
    auditRefund(data) {
      return Promise.resolve({})
    }
  }
}

export default mockApi
