package com.goaltracker.service;

import com.goaltracker.entity.Domain;
import com.goaltracker.entity.Reward;
import com.goaltracker.repository.DomainRepository;
import com.goaltracker.repository.RewardRepository;
import org.springframework.stereotype.Service;

/**
 * 新帳號註冊後的「開箱體驗」——如果一註冊完畫面整個空空的,
 * 新使用者常常不知道從哪裡開始。這裡自動給 2 個範例面向 + 3 個範例獎勵,
 * 讓新帳號一登入就看得懂「這個系統大概長什麼樣子」,可以自己刪掉改成想要的。
 */
@Service
public class OnboardingService {

    private final DomainRepository domainRepository;
    private final RewardRepository rewardRepository;

    public OnboardingService(DomainRepository domainRepository, RewardRepository rewardRepository) {
        this.domainRepository = domainRepository;
        this.rewardRepository = rewardRepository;
    }

    public void seedStarterData(Long userId) {
        Domain work = new Domain();
        work.setUserId(userId);
        work.setName("工作");
        work.setColor("accent");
        work.setSortOrder(0);
        domainRepository.save(work);

        Domain life = new Domain();
        life.setUserId(userId);
        life.setName("生活");
        life.setColor("warning");
        life.setSortOrder(1);
        domainRepository.save(life);

        saveReward(userId, "追一集喜歡的劇", 30);
        saveReward(userId, "買一杯手搖飲", 50);
        saveReward(userId, "耍廢半小時不內疚", 20);
    }

    private void saveReward(Long userId, String title, int cost) {
        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setTitle(title);
        reward.setCost(cost);
        rewardRepository.save(reward);
    }
}
