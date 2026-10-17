package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Favorite;
import com.yanleaf.service.FavoriteService;
import com.yanleaf.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【favorite(商品收藏表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{

}




