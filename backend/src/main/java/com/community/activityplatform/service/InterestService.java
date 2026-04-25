package com.community.activityplatform.service;

import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.Interest;
import com.community.activityplatform.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 兴趣标签服务
 */
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;

    /**
     * 获取所有兴趣标签
     */
    public Result<List<InterestDTO>> getAllInterests() {
        List<Interest> interests = interestRepository.findByActiveTrueOrderByPopularityDesc();
        List<InterestDTO> interestDTOs = interests.stream()
                .map(interest -> InterestDTO.builder()
                        .id(interest.getId())
                        .name(interest.getName())
                        .description(interest.getDescription())
                        .category(interest.getCategory())
                        .popularity(interest.getPopularity())
                        .active(interest.getActive())
                        .build())
                .collect(Collectors.toList());

        return Result.success(interestDTOs);
    }

    /**
     * 按分类获取兴趣标签
     */
    public Result<List<InterestDTO>> getInterestsByCategory(String category) {
        List<Interest> interests = interestRepository.findByCategoryAndActiveTrueOrderByPopularityDesc(category);
        List<InterestDTO> interestDTOs = interests.stream()
                .map(interest -> InterestDTO.builder()
                        .id(interest.getId())
                        .name(interest.getName())
                        .description(interest.getDescription())
                        .category(interest.getCategory())
                        .popularity(interest.getPopularity())
                        .active(interest.getActive())
                        .build())
                .collect(Collectors.toList());

        return Result.success(interestDTOs);
    }

    /**
     * 创建兴趣标签(仅管理员)
     */
    public Result<InterestDTO> createInterest(String name, String description, String category) {
        if (interestRepository.findByName(name).isPresent()) {
            return Result.error("兴趣标签已存在");
        }

        Interest interest = Interest.builder()
                .name(name)
                .description(description)
                .category(category)
                .popularity(0)
                .active(true)
                .build();

        interestRepository.save(interest);

        return Result.success(InterestDTO.builder()
                .id(interest.getId())
                .name(interest.getName())
                .description(interest.getDescription())
                .category(interest.getCategory())
                .popularity(interest.getPopularity())
                .active(interest.getActive())
                .build());
    }
}
