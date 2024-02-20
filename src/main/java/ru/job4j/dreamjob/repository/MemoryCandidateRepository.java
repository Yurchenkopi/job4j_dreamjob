package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
@Repository
public class MemoryCandidateRepository implements CandidateRepository {

    private final AtomicInteger nextId = new AtomicInteger(1);

    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Ivan Ivanov", "<1 года опыта", LocalDateTime.now(), 1));
        save(new Candidate(0, "Pavel Pavlov", ">3 лет опыта", LocalDateTime.now(), 2));
        save(new Candidate(0, "Petya Petrov", "Писать код - мое призвание", LocalDateTime.now(), 2));
        save(new Candidate(0, "Alex Alexeev", ">10 лет", LocalDateTime.now(), 3));
        save(new Candidate(0, "Fedya Fedorov", ">10 лет", LocalDateTime.now(), 1));
        save(new Candidate(0, "Roma Romanov", "5 лет опыта", LocalDateTime.now(), 3));
    }

    @Override
    public Candidate save(Candidate candidate) {
        candidate.setId(nextId.getAndIncrement());
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id) != null;
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(
                        oldCandidate.getId(), candidate.getName(), candidate.getDescription(),
                        candidate.getCreationDate(), candidate.getCityId()
                )) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}
