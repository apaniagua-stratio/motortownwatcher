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

        //EntityManager entityManager = entityManagerFactory().createEntityManager();

        //EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            //entityTransaction.begin();

            for (int i = 0; i < rows.size(); i++) {
               // if (i > 0 && i % batchSize == 0) {
                    //entityTransaction.commit();
                    //entityTransaction.begin();

                    //entityManager.clear();
                    //entityManager.flush();
                //}

                //Post post = new Post(String.format("Post %d", i + 1));

                entityManager.persist(rows.get(i));
                result++;
                System.out.println("persited: " + result);
            }

            return result;
            //entityTransaction.commit();
        } catch (RuntimeException e) {
            /*
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            */

            throw e;
        } finally {
            entityManager.close();
        }

    }


}