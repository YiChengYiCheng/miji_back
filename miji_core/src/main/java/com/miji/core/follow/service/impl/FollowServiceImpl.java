package com.miji.core.follow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.FollowDO;
import com.common.DO.UserDO;
import com.common.QO.follow.FollowListQO;
import com.common.QO.follow.FollowQO;
import com.common.VO.follow.FollowUserVO;
import com.common.enums.CodeEnum;
import com.common.enums.DefaultValue;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.core.follow.mapper.FollowMapper;
import com.miji.core.follow.service.FollowService;
import com.miji.core.notification.service.NotificationService;
import com.miji.core.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result add(FollowQO qo, Long currentUserId) {
        Long followUserId = qo.getFollowUserId();
        checkLoginUser(currentUserId);
        if (currentUserId.equals(followUserId)) {
            throw new CustomException(HttpServletResponse.SC_BAD_REQUEST, "不能关注自己");
        }
        checkActiveUser(followUserId);

        if (exists(currentUserId, followUserId)) {
            return Result.success(true);
        }

        FollowDO followDO = new FollowDO();
        followDO.setUserId(currentUserId);
        followDO.setFollowUserId(followUserId);
        followDO.setCreateTime(LocalDateTime.now());

        try {
            int insert = followMapper.insert(followDO);
            if (insert <= 0) {
                return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_INSERT_FAIL.getStatusCode(), "follow insert fail");
            }
        } catch (DuplicateKeyException e) {
            return Result.success(true);
        }

        increaseFollowCount(currentUserId);
        increaseFansCount(followUserId);
        notificationService.notifyFollow(currentUserId, followUserId);
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancel(FollowQO qo, Long currentUserId) {
        Long followUserId = qo.getFollowUserId();
        checkLoginUser(currentUserId);
        if (currentUserId.equals(followUserId)) {
            throw new CustomException(HttpServletResponse.SC_BAD_REQUEST, "can not unfollow yourself");
        }

        int delete = followMapper.delete(new LambdaQueryWrapper<FollowDO>()
                .eq(FollowDO::getUserId, currentUserId)
                .eq(FollowDO::getFollowUserId, followUserId));
        if (delete <= 0) {
            return Result.success(true);
        }

        decreaseFollowCount(currentUserId);
        decreaseFansCount(followUserId);
        return Result.success(true);
    }

    @Override
    public Result status(FollowQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        return Result.success(exists(currentUserId, qo.getFollowUserId()));
    }

    @Override
    public Result followingList(FollowListQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        Long targetUserId = getTargetUserId(qo, currentUserId);
        Page<FollowDO> page = followMapper.selectPage(new Page<>(getPage(qo), getSize(qo)),
                new LambdaQueryWrapper<FollowDO>()
                        .eq(FollowDO::getUserId, targetUserId)
                        .orderByDesc(FollowDO::getCreateTime));
        List<Long> userIds = page.getRecords().stream()
                .map(FollowDO::getFollowUserId)
                .collect(Collectors.toList());
        return Result.success(buildUserPage(page, userIds, currentUserId));
    }

    @Override
    public Result fansList(FollowListQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        Long targetUserId = getTargetUserId(qo, currentUserId);
        Page<FollowDO> page = followMapper.selectPage(new Page<>(getPage(qo), getSize(qo)),
                new LambdaQueryWrapper<FollowDO>()
                        .eq(FollowDO::getFollowUserId, targetUserId)
                        .orderByDesc(FollowDO::getCreateTime));
        List<Long> userIds = page.getRecords().stream()
                .map(FollowDO::getUserId)
                .collect(Collectors.toList());
        return Result.success(buildUserPage(page, userIds, currentUserId));
    }

    private boolean exists(Long userId, Long followUserId) {
        Long count = followMapper.selectCount(new LambdaQueryWrapper<FollowDO>()
                .eq(FollowDO::getUserId, userId)
                .eq(FollowDO::getFollowUserId, followUserId));
        return count != null && count > 0;
    }

    private void checkLoginUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new CustomException(HttpServletResponse.SC_UNAUTHORIZED, "please login first");
        }
    }

    private void checkActiveUser(Long userId) {
        UserDO userDO = userMapper.selectById(userId);
        if (userDO == null || !DefaultValue.DEFAULT_STATUS.equals(userDO.getStatus())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "user not found or inactive");
        }
    }

    private Long getTargetUserId(FollowListQO qo, Long currentUserId) {
        if (qo == null || qo.getUserId() == null) {
            return currentUserId;
        }
        return qo.getUserId();
    }

    private int getPage(FollowListQO qo) {
        return qo == null || qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
    }

    private int getSize(FollowListQO qo) {
        return qo == null || qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();
    }

    private Page<FollowUserVO> buildUserPage(Page<FollowDO> followPage, List<Long> userIds, Long currentUserId) {
        Page<FollowUserVO> result = new Page<>(followPage.getCurrent(), followPage.getSize(), followPage.getTotal());
        if (userIds.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        Map<Long, UserDO> userMap = userMapper.selectBatchIds(userIds).stream()
                .filter(user -> DefaultValue.DEFAULT_STATUS.equals(user.getStatus()))
                .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        Set<Long> followedUserIds = selectFollowedUserIds(currentUserId, userIds);
        List<FollowUserVO> records = userIds.stream()
                .map(userMap::get)
                .filter(user -> user != null)
                .map(user -> buildFollowUserVO(user, followedUserIds.contains(user.getId())))
                .collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    private Set<Long> selectFollowedUserIds(Long currentUserId, List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return followMapper.selectList(new LambdaQueryWrapper<FollowDO>()
                        .eq(FollowDO::getUserId, currentUserId)
                        .in(FollowDO::getFollowUserId, userIds))
                .stream()
                .map(FollowDO::getFollowUserId)
                .collect(Collectors.toSet());
    }

    private FollowUserVO buildFollowUserVO(UserDO userDO, boolean followed) {
        FollowUserVO vo = new FollowUserVO();
        vo.setId(userDO.getId());
        vo.setNickname(userDO.getNickname());
        vo.setAvatar(userDO.getAvatar());
        vo.setBio(userDO.getBio());
        vo.setFansCount(userDO.getFansCount());
        vo.setFollowCount(userDO.getFollowCount());
        vo.setFollowed(followed);
        return vo;
    }

    private void increaseFollowCount(Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .setSql("follow_count = IFNULL(follow_count, 0) + 1"));
    }

    private void decreaseFollowCount(Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .gt(UserDO::getFollowCount, DefaultValue.NUM_ZERO)
                .setSql("follow_count = follow_count - 1"));
    }

    private void increaseFansCount(Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .setSql("fans_count = IFNULL(fans_count, 0) + 1"));
    }

    private void decreaseFansCount(Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .gt(UserDO::getFansCount, DefaultValue.NUM_ZERO)
                .setSql("fans_count = fans_count - 1"));
    }
}
