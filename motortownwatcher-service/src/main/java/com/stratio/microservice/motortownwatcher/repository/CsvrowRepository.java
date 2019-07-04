package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.CsvRow;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

@Repository
public class CsvrowRepository extends SimpleJpaRepository<CsvRow, Long> {
    private EntityManager entityManager;
    public CsvrowRepository(EntityManager entityManager) {
        super(CsvRow.class, entityManager);
        this.entityManager=entityManager;
    }

    public List<CsvRow> findByEntity(String entity) {
        return this.findAll();
    }

    @Transactional
    public List<CsvRow> save(List<CsvRow> rows) {
        rows.forEach(row -> entityManager.persist(row));
        //entityManager.flush();
        return rows;
    }

    @Transactional
    public long batchSave(List<CsvRow> rows) {
        int entityCount = 50;
        int batchSize = 25;
        long result=0;

        try {
            //entityTransaction.begin();

            for (int i = 0; i < rows.size(); i++) {

                entityManager.persist(rows.get(i));
                result++;
                System.out.println("persited: " + result);
            }

            return result;

        } catch (RuntimeException e) {

            throw e;
        } finally {
            entityManager.close();
        }

    }


}