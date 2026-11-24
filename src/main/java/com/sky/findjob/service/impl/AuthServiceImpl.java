package com.sky.findjob.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.findjob.common.BusinessException;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.dto.*;
import com.sky.findjob.entity.User;
import com.sky.findjob.mapper.UserMapper;
import com.sky.findjob.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sky.findjob.utils.JwtUtil;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        // 1. 邮箱去空格并转小写
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        // 2. 查询邮箱是否已存在
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, email)
        );

        // 3. 已存在则返回 409
        if (count != null && count > 0) {
            throw new BusinessException("邮箱已注册", HttpStatus.CONFLICT);
        }

        // 4. 创建用户
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);

        // 5. 密码加密
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // 6. 新用户默认资料
        user.setSchool("");
        user.setMajor("");
        user.setGraduationYear(2027);
        user.setTargetRole("");
        user.setLocation("");
        user.setBio("");
        user.setWeeklyGoal(8);

        // 7. 插入数据库
        userMapper.insert(user);

        // 8. 返回不包含密码的响应对象
        return UserResponse.from(user);
    }
    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getEmail, email));

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("邮箱或密码错误", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.createToken(user.getId(), user.getEmail());
        return LoginResponse.of(token, jwtUtil.getExpireMillis(), UserResponse.from(user));
    }

    @Override
    public UserResponse getCurrentUser() {
        Long userId = UserContext.require();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED);
        }
        return UserResponse.from(user);
    }

    @Override
    public Object updateProfile(UpdateProfileRequest request) {
        Long userId = UserContext.require();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED);
        }
        user.setName(request.name().trim());
        user.setSchool(request.school().trim());
        user.setMajor(request.major().trim());
        user.setGraduationYear(request.graduationYear());
        user.setTargetRole(request.targetRole().trim());
        user.setLocation(request.location().trim());
        user.setBio(request.bio().trim());
        user.setWeeklyGoal(request.weeklyGoal());
        userMapper.updateById(user);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void updatePassword(ChangePasswordRequest request) {
        Long userId = UserContext.require();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED);
        }

        // 1. 校验原密码
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码不正确", HttpStatus.BAD_REQUEST);
        }

        // 2. 两次新密码一致
//        if (!request.newPassword().equals(request.confirmPassword())) {
//            throw new BusinessException("两次输入的新密码不一致", HttpStatus.BAD_REQUEST);
//        }

        // 2. 前端如果传了确认密码才校验一致，没传则跳过
        if (request.confirmPassword() != null
                && !request.confirmPassword().isBlank()
                && !request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致", HttpStatus.BAD_REQUEST);
        }
// ... existing code ...

        // 3. 新密码不能与原密码相同
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("新密码不能与原密码相同", HttpStatus.BAD_REQUEST);
        }

        // 4. 只更新密码字段，避免覆盖其它并发修改的业务字段
        User update = new User();
        update.setId(userId);
        update.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(update);
    }


}
