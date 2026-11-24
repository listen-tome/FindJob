package com.sky.findjob.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.findjob.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

// ... existing code ...
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
