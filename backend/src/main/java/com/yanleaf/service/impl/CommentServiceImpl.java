package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Comment;
import com.yanleaf.service.CommentService;
import com.yanleaf.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【comment(商品评论表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




