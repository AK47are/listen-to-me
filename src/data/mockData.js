// 模拟数据
const mockData = {
  // 登录响应
  login: {
    data: {
      token: 'mock-token-123456',
      userInfo: {
        id: 1,
        username: 'testuser',
        nickname: '测试用户',
        avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
        phone: '13800138000',
        email: 'test@example.com',
        userType: 'LISTENER'
      }
    }
  },

  // 听众用户登录响应
  listenerLogin: {
    data: {
      token: 'mock-token-listener-123456',
      userInfo: {
        id: 1001,
        username: 'listener',
        nickname: '听众用户',
        avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
        phone: '13800138001',
        email: 'listener@example.com',
        userType: 'LISTENER'
      }
    }
  },

  // 创作者用户登录响应
  creatorLogin: {
    data: {
      token: 'mock-token-creator-123456',
      userInfo: {
        id: 88,
        username: 'creator',
        nickname: '后端架构师',
        avatar: 'https://randomuser.me/api/portraits/men/45.jpg',
        phone: '13800138002',
        email: 'creator@example.com',
        userType: 'CREATOR'
      }
    }
  },

  // 注册响应
  register: {
    code: 200,
    msg: '注册成功'
  },

  // 个人信息
  profile: {
    id: 1,
    username: 'testuser',
    nickname: '测试用户',
    avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    phone: '13800138000',
    email: 'test@example.com'
  },

  // 热门音频
  hotList: [
    {
      id: 1,
      title: 'Spring Boot 核心原理解析',
      coverUrl: 'https://picsum.photos/400/400?random=1',
      duration: 3600,
      creatorName: '后端架构师',
      playCount: 1205,
      hotScore: 9850
    },
    {
      id: 2,
      title: 'Vue 3 组合式 API 实战',
      coverUrl: 'https://picsum.photos/400/400?random=2',
      duration: 2400,
      creatorName: '前端专家',
      playCount: 980,
      hotScore: 8760
    },
    {
      id: 3,
      title: 'React 性能优化技巧',
      coverUrl: 'https://picsum.photos/400/400?random=3',
      duration: 1800,
      creatorName: '前端架构师',
      playCount: 850,
      hotScore: 7650
    },
    {
      id: 4,
      title: '微服务架构设计',
      coverUrl: 'https://picsum.photos/400/400?random=4',
      duration: 4200,
      creatorName: '系统架构师',
      playCount: 720,
      hotScore: 6540
    },
    {
      id: 5,
      title: 'Docker 容器化实践',
      coverUrl: 'https://picsum.photos/400/400?random=5',
      duration: 2700,
      creatorName: 'DevOps 专家',
      playCount: 680,
      hotScore: 5430
    }
  ],

  // 推荐音频
  recommendList: {
    records: [
      {
        id: 6,
        title: 'Node.js 高级编程',
        coverUrl: 'https://picsum.photos/400/400?random=6',
        duration: 3000,
        creatorName: '全栈开发者',
        playCount: 520,
        collectCount: 120
      },
      {
        id: 7,
        title: 'TypeScript 实战指南',
        coverUrl: 'https://picsum.photos/400/400?random=7',
        duration: 2100,
        creatorName: '前端专家',
        playCount: 480,
        collectCount: 95
      },
      {
        id: 8,
        title: 'Redis 高性能缓存',
        coverUrl: 'https://picsum.photos/400/400?random=8',
        duration: 3300,
        creatorName: '数据库专家',
        playCount: 420,
        collectCount: 85
      },
      {
        id: 9,
        title: 'Kubernetes 集群管理',
        coverUrl: 'https://picsum.photos/400/400?random=9',
        duration: 4500,
        creatorName: 'DevOps 专家',
        playCount: 380,
        collectCount: 75
      }
    ],
    total: 4
  },

  // 音频详情
  audioDetail: {
    id: 1,
    title: 'Spring Boot 核心原理解析',
    coverUrl: 'https://picsum.photos/400/400?random=1',
    duration: 3600,
    trialDuration: 30,
    isPaid: true,
    price: 19.90,
    isPurchased: true,
    progress: 120,
    creator: {
      id: 88,
      nickname: '后端架构师',
      avatar: 'https://randomuser.me/api/portraits/men/45.jpg'
    },
    stats: {
      playCount: 1205,
      collectCount: 340,
      likeCount: 890
    },
    description: '本课程深入讲解 Spring Boot 的核心原理，包括自动配置、启动流程、依赖管理等内容，帮助开发者更好地理解和使用 Spring Boot 框架。'
  },

  // 收藏夹列表
  folderList: [
    {
      folderId: 1,
      name: '默认收藏夹',
      description: '默认收藏夹',
      audioCount: 5,
      createTime: 1711425600000
    },
    {
      folderId: 2,
      name: '前端技术',
      description: '前端相关课程',
      audioCount: 3,
      createTime: 1711512000000
    },
    {
      folderId: 3,
      name: '后端技术',
      description: '后端相关课程',
      audioCount: 4,
      createTime: 1711598400000
    }
  ],

  // 收藏音频列表
  favoriteList: {
    records: [
      {
        id: 1,
        title: 'Spring Boot 核心原理解析',
        coverUrl: 'https://picsum.photos/400/400?random=1',
        duration: 3600,
        creatorName: '后端架构师',
        playCount: 1205,
        collectCount: 340
      },
      {
        id: 2,
        title: 'Vue 3 组合式 API 实战',
        coverUrl: 'https://picsum.photos/400/400?random=2',
        duration: 2400,
        creatorName: '前端专家',
        playCount: 980,
        collectCount: 280
      }
    ],
    total: 2
  },

  // 喜欢列表
  likeList: {
    records: [
      {
        id: 1,
        title: 'Spring Boot 核心原理解析',
        coverUrl: 'https://picsum.photos/400/400?random=1',
        duration: 3600,
        creatorName: '后端架构师',
        playCount: 1205,
        collectCount: 340
      },
      {
        id: 3,
        title: 'React 性能优化技巧',
        coverUrl: 'https://picsum.photos/400/400?random=3',
        duration: 1800,
        creatorName: '前端架构师',
        playCount: 850,
        collectCount: 210
      }
    ],
    total: 2
  },

  // 历史记录
  historyList: {
    records: [
      {
        id: 1,
        title: 'Spring Boot 核心原理解析',
        coverUrl: 'https://picsum.photos/400/400?random=1',
        duration: 3600,
        creatorName: '后端架构师',
        playCount: 1205,
        createTime: 1711425600000
      },
      {
        id: 2,
        title: 'Vue 3 组合式 API 实战',
        coverUrl: 'https://picsum.photos/400/400?random=2',
        duration: 2400,
        creatorName: '前端专家',
        playCount: 980,
        createTime: 1711512000000
      }
    ],
    total: 2
  },

  // 评论列表
  commentList: {
    records: [
      {
        id: 1,
        content: '这门课程非常棒，讲解清晰，收获很大！',
        createTime: 1711425600000,
        user: {
          id: 2,
          nickname: '学习达人',
          avatar: 'https://randomuser.me/api/portraits/women/22.jpg'
        },
        likeCount: 15,
        isLiked: false,
        isOwn: false,
        replies: [
          {
            id: 2,
            content: '是的，老师讲得很详细',
            createTime: 1711432800000,
            user: {
              id: 3,
              nickname: '技术爱好者',
              avatar: 'https://randomuser.me/api/portraits/men/25.jpg'
            },
            likeCount: 5,
            isLiked: false,
            replyTo: {
              id: 2,
              nickname: '学习达人'
            }
          }
        ]
      }
    ],
    total: 1
  },

  // 创作者稿件列表
  creatorAudioList: {
    records: [
      {
        id: 1,
        title: 'Spring Boot 核心原理解析',
        coverUrl: 'https://picsum.photos/400/400?random=1',
        duration: 3600,
        price: 19.90,
        isPaid: true,
        visibility: 'PUBLIC',
        status: 'APPROVED',
        playCount: 1205,
        createTime: 1711425600000
      },
      {
        id: 6,
        title: 'Node.js 高级编程',
        coverUrl: 'https://picsum.photos/400/400?random=6',
        duration: 3000,
        price: 0,
        isPaid: false,
        visibility: 'PUBLIC',
        status: 'APPROVED',
        playCount: 520,
        createTime: 1711512000000
      }
    ],
    total: 2
  },

  // 钱包信息
  balance: {
    balance: 2801,
    totalRecharge: 5000,
    totalSpent: 2199
  },

  // 订单列表
  orderList: {
    records: [
      {
        orderSn: 'ORD202603280001',
        orderType: 'PURCHASE',
        audioId: 1,
        audioTitle: 'Spring Boot 核心原理解析',
        coverUrl: 'https://picsum.photos/400/400?random=1',
        payAmount: 199,
        status: 'SUCCESS',
        createTime: 1711425600000
      },
      {
        orderSn: 'RC202603280001',
        orderType: 'RECHARGE',
        payAmount: 1000,
        status: 'SUCCESS',
        createTime: 1711339200000
      }
    ],
    total: 2
  },

  // 时间槽列表
  slotList: {
    records: [
      {
        id: 1,
        startTime: '2026-04-01 10:00:00',
        endTime: '2026-04-01 11:00:00',
        price: 50,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 2,
        startTime: '2026-04-01 14:00:00',
        endTime: '2026-04-01 15:00:00',
        price: 50,
        address: '腾讯会议链接xxx',
        status: 'BOOKED'
      },
      {
        id: 3,
        startTime: '2026-04-02 10:00:00',
        endTime: '2026-04-02 11:00:00',
        price: 50,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 4,
        startTime: '2026-04-02 14:00:00',
        endTime: '2026-04-02 15:00:00',
        price: 80,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 5,
        startTime: '2026-04-03 09:00:00',
        endTime: '2026-04-03 10:00:00',
        price: 50,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 6,
        startTime: '2026-04-03 14:00:00',
        endTime: '2026-04-03 15:00:00',
        price: 60,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 7,
        startTime: '2026-04-04 10:00:00',
        endTime: '2026-04-04 11:00:00',
        price: 50,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 8,
        startTime: '2026-04-04 16:00:00',
        endTime: '2026-04-04 17:00:00',
        price: 100,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      },
      {
        id: 9,
        startTime: '2026-04-05 10:00:00',
        endTime: '2026-04-05 11:00:00',
        price: 50,
        address: '腾讯会议链接xxx',
        status: 'CANCELLED'
      },
      {
        id: 10,
        startTime: '2026-04-05 14:00:00',
        endTime: '2026-04-05 15:00:00',
        price: 70,
        address: '腾讯会议链接xxx',
        status: 'AVAILABLE'
      }
    ],
    total: 10
  },

  // 创作者预约订单列表
  consultOrderList: {
    records: [
      {
        id: 1,
        slotId: 1,
        date: '2026-04-01',
        startTime: '10:00',
        endTime: '11:00',
        price: 50,
        status: 'PENDING_CONFIRM',
        address: '腾讯会议链接xxx',
        userId: 1001,
        userNickname: '听众_8848',
        userAvatar: 'https://randomuser.me/api/portraits/men/32.jpg',
        message: '想咨询一下技术问题',
        createTime: 1711425600000
      },
      {
        id: 2,
        slotId: 2,
        date: '2026-04-01',
        startTime: '14:00',
        endTime: '15:00',
        price: 50,
        status: 'CONFIRMED',
        address: '腾讯会议链接xxx',
        userId: 1002,
        userNickname: '技术爱好者',
        userAvatar: 'https://randomuser.me/api/portraits/women/22.jpg',
        message: '想了解一下架构设计',
        createTime: 1711432800000
      }
    ],
    total: 2
  },

  // 用户预约订单列表
  userConsultOrderList: {
    records: [
      {
        id: 1,
        slotId: 1,
        date: '2026-04-01',
        startTime: '10:00',
        endTime: '11:00',
        price: 50,
        status: 'PENDING_CONFIRM',
        address: null,
        creatorId: 88,
        creatorName: '后端架构师',
        creatorAvatar: 'https://randomuser.me/api/portraits/men/45.jpg',
        message: '想咨询一下技术问题',
        createTime: 1711425600000
      },
      {
        id: 3,
        slotId: 3,
        date: '2026-04-02',
        startTime: '10:00',
        endTime: '11:00',
        price: 50,
        status: 'COMPLETED',
        address: '腾讯会议链接xxx',
        creatorId: 88,
        creatorName: '后端架构师',
        creatorAvatar: 'https://randomuser.me/api/portraits/men/45.jpg',
        message: '想了解一下微服务架构',
        createTime: 1711512000000
      }
    ],
    total: 2
  },

  // 创作者列表
  creatorList: [
    {
      id: 88,
      nickname: '后端架构师',
      avatar: 'https://randomuser.me/api/portraits/men/45.jpg',
      title: '资深后端工程师',
      bio: '10年后端开发经验，擅长微服务架构设计、分布式系统、高并发处理。曾任职于多家知名互联网公司，有丰富的技术咨询经验。',
      tags: ['Java', 'Spring Boot', '微服务', '架构设计'],
      audioCount: 12,
      followerCount: 2560,
      minPrice: 50,
      isOnline: true
    },
    {
      id: 89,
      nickname: '前端专家',
      avatar: 'https://randomuser.me/api/portraits/women/33.jpg',
      title: '高级前端工程师',
      bio: '8年前端开发经验，精通Vue、React等主流框架，专注于前端性能优化和工程化建设。',
      tags: ['Vue', 'React', '前端工程化', '性能优化'],
      audioCount: 8,
      followerCount: 1890,
      minPrice: 60,
      isOnline: true
    },
    {
      id: 90,
      nickname: 'AI算法工程师',
      avatar: 'https://randomuser.me/api/portraits/men/28.jpg',
      title: '人工智能专家',
      bio: '机器学习博士，专注于自然语言处理和计算机视觉领域，有多篇顶会论文发表经验。',
      tags: ['机器学习', '深度学习', 'NLP', '计算机视觉'],
      audioCount: 15,
      followerCount: 3200,
      minPrice: 100,
      isOnline: false
    },
    {
      id: 91,
      nickname: '产品经理老王',
      avatar: 'https://randomuser.me/api/portraits/men/52.jpg',
      title: '资深产品经理',
      bio: '12年产品经验，从0到1主导过多个千万级用户产品，擅长产品规划和用户增长策略。',
      tags: ['产品设计', '用户增长', '数据分析', '商业思维'],
      audioCount: 20,
      followerCount: 4100,
      minPrice: 80,
      isOnline: true
    },
    {
      id: 92,
      nickname: '运维大牛',
      avatar: 'https://randomuser.me/api/portraits/men/36.jpg',
      title: 'DevOps专家',
      bio: '10年运维经验，精通Kubernetes、Docker等云原生技术，帮助企业实现自动化运维转型。',
      tags: ['Kubernetes', 'Docker', 'CI/CD', '云原生'],
      audioCount: 6,
      followerCount: 1450,
      minPrice: 70,
      isOnline: false
    },
    {
      id: 93,
      nickname: '数据分析师Lisa',
      avatar: 'https://randomuser.me/api/portraits/women/44.jpg',
      title: '数据分析专家',
      bio: '统计学硕士，擅长数据挖掘和商业分析，帮助多家企业建立数据驱动决策体系。',
      tags: ['数据分析', 'Python', 'SQL', '商业智能'],
      audioCount: 10,
      followerCount: 2100,
      minPrice: 55,
      isOnline: true
    }
  ],

  // 管理员登录响应
  adminLogin: {
    data: {
      token: 'mock-token-admin-123456',
      userInfo: {
        id: 0,
        username: 'admin',
        nickname: '管理员',
        avatar: 'https://randomuser.me/api/portraits/men/68.jpg',
        phone: '13800138009',
        email: 'admin@example.com',
        userType: 'ADMIN'
      }
    }
  },

  // 待审音频列表
  auditAudioList: {
    records: [
      {
        id: 1001,
        title: '如何成为一名优秀的程序员',
        coverUrl: 'https://picsum.photos/400/400?random=10',
        audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
        duration: 2400,
        creatorId: 88,
        creatorName: '后端架构师',
        description: '本课程深入讲解如何成为一名优秀的程序员，包括编程思维、代码规范、持续学习等方面内容。',
        transcript: '大家好，今天我要分享的是如何成为一名优秀的程序员。首先，我们需要培养良好的编程思维，这包括抽象思维、逻辑思维和系统思维。其次，代码规范非常重要，良好的代码规范不仅能让你的代码更易读，也能提高团队协作效率。最后，持续学习是程序员的必修课，技术在不断更新，我们需要保持学习的热情。',
        submitTime: 1711425600000
      },
      {
        id: 1002,
        title: '前端性能优化实战',
        coverUrl: 'https://picsum.photos/400/400?random=11',
        audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
        duration: 1800,
        creatorId: 89,
        creatorName: '前端专家',
        description: '前端性能优化是一个重要的话题，本课程将探讨一些实战技巧，帮助你提升网站性能。',
        transcript: '前端性能优化是一个重要的话题，今天我们来探讨一些实战技巧。首先，我们要关注资源加载优化，包括图片懒加载、代码分割、CDN加速等。其次，渲染性能也很关键，我们要避免重排和重绘，使用CSS硬件加速。最后，缓存策略能显著提升用户体验，合理使用浏览器缓存和服务端缓存。',
        submitTime: 1711512000000
      }
    ],
    total: 2
  },

  // 待审申请列表
  auditApplyList: {
    records: [
      {
        applyId: 1,
        userId: 1001,
        username: '听众_8848',
        realName: '张三',
        phone: '13800000000',
        intro: '资深后端工程师，擅长技术分享',
        attachment: 'https://picsum.photos/400/400?random=20',
        applyTime: 1711425600000
      },
      {
        applyId: 2,
        userId: 1002,
        username: '技术爱好者',
        realName: '李四',
        phone: '13800000001',
        intro: '前端开发工程师，有丰富的项目经验',
        attachment: 'https://picsum.photos/400/400?random=21',
        applyTime: 1711512000000
      }
    ],
    total: 2
  },

  // 全站数据大盘
  dashboard: {
    totalSales: 285000.00,
    todaySales: 1500.00,
    activeUsers: 12000,
    totalUsers: 50000,
    monthlyActiveUsers: 25000,
    conversionRate: 0.085,
    salesTrend: [
      { date: '2026-03-20', amount: 1200 },
      { date: '2026-03-21', amount: 1800 },
      { date: '2026-03-22', amount: 1500 },
      { date: '2026-03-23', amount: 2000 },
      { date: '2026-03-24', amount: 1700 },
      { date: '2026-03-25', amount: 1900 },
      { date: '2026-03-26', amount: 2200 }
    ],
    hotAudioList: [
      { id: 1, title: 'Spring Boot 核心原理解析', playCount: 1205 },
      { id: 2, title: 'Vue 3 组合式 API 实战', playCount: 980 },
      { id: 3, title: 'React 性能优化技巧', playCount: 850 },
      { id: 4, title: '微服务架构设计', playCount: 720 },
      { id: 5, title: 'Docker 容器化实践', playCount: 680 }
    ],
    creatorEarnings: [
      { creatorName: '后端架构师', amount: 5000 },
      { creatorName: '前端专家', amount: 3200 },
      { creatorName: 'AI算法工程师', amount: 2800 },
      { creatorName: '产品经理老王', amount: 2500 },
      { creatorName: '数据分析师Lisa', amount: 1800 }
    ]
  },

  // 退款申请列表
  refundApplyList: {
    records: [
      {
        id: 1,
        orderId: 1,
        slotId: 1,
        date: '2026-04-01',
        startTime: '10:00',
        endTime: '11:00',
        price: 50,
        orderStatus: 'CONFIRMED',
        userId: 1001,
        userNickname: '听众_8848',
        userAvatar: 'https://randomuser.me/api/portraits/men/32.jpg',
        creatorId: 88,
        creatorName: '后端架构师',
        reason: '咨询质量不好，未解决我的问题',
        applyTime: 1711425600000
      },
      {
        id: 2,
        orderId: 3,
        slotId: 3,
        date: '2026-04-02',
        startTime: '10:00',
        endTime: '11:00',
        price: 50,
        orderStatus: 'COMPLETED',
        userId: 1002,
        userNickname: '技术爱好者',
        userAvatar: 'https://randomuser.me/api/portraits/women/22.jpg',
        creatorId: 88,
        creatorName: '后端架构师',
        reason: '预约时间冲突，无法参加',
        applyTime: 1711512000000
      }
    ],
    total: 2
  }
}

export default mockData
