package com.goaltracker.service;

import com.goaltracker.dto.CoinBalanceResponse;
import com.goaltracker.dto.RewardRequest;
import com.goaltracker.dto.RewardResponse;
import com.goaltracker.entity.Reward;
import com.goaltracker.exception.ResourceNotFoundException;
import com.goaltracker.repository.RewardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final CoinService coinService;

    public RewardService(RewardRepository rewardRepository, CoinService coinService) {
        this.rewardRepository = rewardRepository;
        this.coinService = coinService;
    }

    public List<RewardResponse> getAll() {
        long balance = coinService.getBalance().balance();
        return rewardRepository.findAllByOrderByCostAsc()
                .stream()
                .map(r -> new RewardResponse(r.getId(), r.getTitle(), r.getCost(), balance >= r.getCost()))
                .toList();
    }

    public RewardResponse create(RewardRequest request) {
        Reward reward = new Reward();
        reward.setTitle(request.title());
        reward.setCost(request.cost());
        Reward saved = rewardRepository.save(reward);
        long balance = coinService.getBalance().balance();
        return new RewardResponse(saved.getId(), saved.getTitle(), saved.getCost(), balance >= saved.getCost());
    }

    public void delete(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的獎勵"));
        rewardRepository.delete(reward);
    }

    // 兌換獎勵:檢查金幣夠不夠,不夠就擋下來給友善訊息,不是讓餘額變負數
    public CoinBalanceResponse redeem(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 id=" + id + " 的獎勵"));

        long balance = coinService.getBalance().balance();
        if (balance < reward.getCost()) {
            long diff = reward.getCost() - balance;
            throw new IllegalArgumentException("金幣不足,還差 " + diff + " 枚才能兌換「" + reward.getTitle() + "」");
        }

        coinService.spend(reward.getCost(), reward.getId(), "兌換:" + reward.getTitle());
        return coinService.getBalance();
    }
}
