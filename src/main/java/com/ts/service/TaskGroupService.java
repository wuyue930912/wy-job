package com.ts.service;

import com.ts.dto.JobDTO;
import com.ts.po.TsJobPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务分组服务
 * 支持任务分组、标签管理，按分组/标签筛选任务
 * 
 * @author wy-job
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskGroupService {

    private final TaskService taskService;
    
    /**
     * 获取所有任务分组
     * 
     * @return 分组列表
     */
    public List<String> getAllJobGroups() {
        // 从已注册的任务和数据库中获取分组
        Set<String> groups = new HashSet<>();
        
        // 从内存中的任务获取分组
        for (JobDTO job : TaskService.allJobs().values()) {
            if (job.getJobGroup() != null && !job.getJobGroup().isEmpty()) {
                groups.add(job.getJobGroup());
            }
        }
        
        return groups.stream().sorted().collect(Collectors.toList());
    }
    
    /**
     * 获取所有任务标签
     * 
     * @return 标签列表
     */
    public List<String> getAllTags() {
        Set<String> tags = new HashSet<>();
        
        // 从内存中的任务获取标签
        for (JobDTO job : TaskService.allJobs().values()) {
            if (job.getTags() != null && !job.getTags().isEmpty()) {
                String[] tagArray = job.getTags().split(",");
                for (String tag : tagArray) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) {
                        tags.add(trimmed);
                    }
                }
            }
        }
        
        return tags.stream().sorted().collect(Collectors.toList());
    }
    
    /**
     * 根据分组获取任务列表
     * 
     * @param group 分组名称
     * @return 任务DTO列表
     */
    public List<JobDTO> getJobsByGroup(String group) {
        if (group == null || group.isEmpty()) {
            return new ArrayList<>();
        }
        
        return TaskService.allJobs().values().stream()
                .filter(job -> group.equals(job.getJobGroup()))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据标签获取任务列表
     * 
     * @param tag 标签名称
     * @return 任务DTO列表
     */
    public List<JobDTO> getJobsByTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return new ArrayList<>();
        }
        
        final String targetTag = tag.trim().toLowerCase();
        return TaskService.allJobs().values().stream()
                .filter(job -> job.getTags() != null && !job.getTags().isEmpty())
                .filter(job -> {
                    String[] tagArray = job.getTags().split(",");
                    for (String t : tagArray) {
                        if (t.trim().toLowerCase().equals(targetTag)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取分组统计信息
     * 
     * @return 分组统计（分组名 -> 任务数量）
     */
    public Map<String, Long> getGroupStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        
        // 初始化已知分组
        List<String> groups = getAllJobGroups();
        for (String group : groups) {
            stats.put(group, 0L);
        }
        
        // 统计每个分组的任务数量
        for (JobDTO job : TaskService.allJobs().values()) {
            String group = job.getJobGroup();
            if (group != null && !group.isEmpty()) {
                stats.put(group, stats.getOrDefault(group, 0L) + 1);
            }
        }
        
        return stats;
    }
    
    /**
     * 获取标签统计信息
     * 
     * @return 标签统计（标签名 -> 任务数量）
     */
    public Map<String, Long> getTagStats() {
        Map<String, Long> stats = new HashMap<>();
        
        for (JobDTO job : TaskService.allJobs().values()) {
            if (job.getTags() != null && !job.getTags().isEmpty()) {
                String[] tagArray = job.getTags().split(",");
                for (String tag : tagArray) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) {
                        stats.put(trimmed, stats.getOrDefault(trimmed, 0L) + 1);
                    }
                }
            }
        }
        
        return stats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * 构建任务分组树
     * 
     * @return 分组树结构
     */
    public List<Map<String, Object>> getGroupTree() {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        Map<String, List<JobDTO>> groupJobs = TaskService.allJobs().values().stream()
                .filter(job -> job.getJobGroup() != null && !job.getJobGroup().isEmpty())
                .collect(Collectors.groupingBy(JobDTO::getJobGroup));
        
        for (Map.Entry<String, List<JobDTO>> entry : groupJobs.entrySet()) {
            Map<String, Object> node = new HashMap<>();
            node.put("group", entry.getKey());
            node.put("count", entry.getValue().size());
            
            List<Map<String, String>> jobs = entry.getValue().stream()
                    .map(job -> {
                        Map<String, String> jobInfo = new HashMap<>();
                        jobInfo.put("key", job.getKey());
                        jobInfo.put("description", job.getDescription());
                        return jobInfo;
                    })
                    .collect(Collectors.toList());
            node.put("jobs", jobs);
            
            tree.add(node);
        }
        
        return tree;
    }
}