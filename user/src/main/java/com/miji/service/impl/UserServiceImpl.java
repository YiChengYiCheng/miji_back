package com.miji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.DO.UserDO;
import com.common.QO.user.LoginQO;
import com.common.QO.user.RefreshTokenQO;
import com.common.VO.user.TokenVO;
import com.common.VO.user.UserVO;
import com.common.enums.CodeEnum;
import com.common.enums.Status;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.mapper.UserMapper;
import com.miji.service.UserService;
import com.miji.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result login(LoginQO qo) {
        if (StringUtils.isEmpty(qo.getAccount()) || StringUtils.isEmpty(qo.getPassword()))
            throw new CustomException(HttpServletResponse.SC_BAD_REQUEST, "用户为空，请重新输入用户名和密码");

        UserDO userDO = userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getAccount, qo.getAccount()));
        if (userDO == null) {
            log.info("登录失败！-->{}", userDO);
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "登录失败，该用户不存在！");
        }
        boolean matches = passwordEncoder.matches(qo.getPassword(), userDO.getPassword());
        if (matches == false) {
            log.info("登录失败,密码错误！-->{}", userDO);
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "登录失败，请重新登录！");
        }
        if (!Status.DEFAULT_STATUS.equals(userDO.getStatus())) {
            log.info("登录失败,用户状态异常！-->{}", userDO);
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "用户状态异常，请联系管理员！");
        }

        UserVO userVO = new UserVO();
        userDO.setPassword(null);
        userVO.setUserInfo(userDO);
        userVO.setAccessToken(jwtUtil.createAccessToken(userDO.getId(), userDO.getAccount()));
        userVO.setRefreshToken(jwtUtil.createRefreshToken(userDO.getId(), userDO.getAccount()));
        userVO.setExpiresIn(jwtUtil.getAccessTokenExpiresIn());

        return Result.success(userVO);
    }

    @Override
    public Result refreshToken(RefreshTokenQO qo) {
        try {
            Claims claims = jwtUtil.parseToken(qo.getRefreshToken());
            if (!jwtUtil.isRefreshToken(claims)) {
                return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "refreshToken类型错误！");
            }

            UserDO userDO = userMapper.selectById(jwtUtil.getUserId(claims));
            if (userDO == null || !Status.DEFAULT_STATUS.equals(userDO.getStatus())) {
                return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "用户不存在或状态异常！");
            }

            TokenVO tokenVO = new TokenVO();
            tokenVO.setAccessToken(jwtUtil.createAccessToken(userDO.getId(), userDO.getAccount()));
            tokenVO.setRefreshToken(jwtUtil.createRefreshToken(userDO.getId(), userDO.getAccount()));
            tokenVO.setExpiresIn(jwtUtil.getAccessTokenExpiresIn());
            return Result.success(tokenVO);
        } catch (JwtException | IllegalArgumentException e) {
            log.info("refreshToken刷新失败！-->{}", qo.getRefreshToken(), e);
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "refreshToken无效或已过期！");
        }
    }
}
