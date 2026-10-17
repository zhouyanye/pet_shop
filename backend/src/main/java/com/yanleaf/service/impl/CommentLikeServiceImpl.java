package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.CommentLike;
import com.yanleaf.service.CommentLikeService;
import com.yanleaf.mapper.CommentLikeMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【comment_like(评论点赞记录表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike>
    implements CommentLikeService{

}




