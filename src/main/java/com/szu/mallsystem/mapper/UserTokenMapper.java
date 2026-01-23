package com.szu.mallsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szu.mallsystem.entity.UserToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {

    @Select("SELECT * FROM user_tokens WHERE jti = #{jti} LIMIT 1")
    UserToken findByJti(String jti);

    @Update("UPDATE user_tokens SET revoked = 1 WHERE jti = #{jti}")
    void revokeByJti(String jti);
}
