package equa.controller;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistanceManager {

    private static final EntityManagerFactory INSTANCE = Persistence.createEntityManagerFactory("equa");

    public static EntityManagerFactory getReference() {
        return INSTANCE;
    }

    public static EntityManager getEntityManager() {
        return INSTANCE.createEntityManager();
    }

    private PersistanceManager() {

    }
}
