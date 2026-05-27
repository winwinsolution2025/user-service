package com.example.userservice.infrastructure.repository;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.repository.UserRepository;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Optional;

import static com.example.jooq.userdb.tables.Users.USERS;

public class MySQLUserRepository implements UserRepository {
    private final DSLContext dbCtx;

    public MySQLUserRepository(DSLContext dbCtx) {
        this.dbCtx = dbCtx;
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return dbCtx.selectFrom(User.TABLE_NAME)
                .where("id=?", id)
                .fetchOptionalInto(User.class);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return dbCtx.selectFrom(User.TABLE_NAME)
                .where("email=?", email)
                .fetchOptionalInto(User.class);
    }

    @Override
    public List<User> getAllUsers() {
        return dbCtx.selectFrom(User.TABLE_NAME)
                .fetchInto(User.class);
    }

    @Override
    public List<User> getUsersByIds(Integer[] ids) {
        return dbCtx.selectFrom(USERS)
                .where(USERS.ID.in(ids))
                .fetchInto(User.class);
    }

    @Override
    public void addUser(User user) {
        var record = dbCtx.insertInto(USERS)
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.ROLE, user.getRole())
                .set(USERS.AVATAR, user.getAvatar())
                .set(USERS.AVATAR_ID, user.getAvatarId())
                .set(USERS.AVATAR_FRAME_ID, user.getAvatarFrameId())
                .set(USERS.BIRTHDATE, user.getBirthdate())
                .set(USERS.GENDER, user.getGender())
                .set(USERS.UUID, user.getUUID())
                .set(USERS.NAME, user.getName())
                .set(USERS.NICKNAME, user.getNickname())
                .returning(USERS.ID)
                .fetchOne();

        if (record == null) {
            throw new RuntimeException("Failed to insert record");
        }
        user.setId(record.get(USERS.ID));
    }

    @Override
    public boolean deleteUser(Integer id) {
        var rows = dbCtx.deleteFrom(USERS)
                .where("id=?", id)
                .execute();

        return rows > 0;
    }

    @Override
    public boolean updateUser(User updatedUser) {
        var rows = dbCtx.update(USERS)
                .set(USERS.NAME, updatedUser.getName())
                .set(USERS.NICKNAME, updatedUser.getNickname())
                .set(USERS.GENDER, updatedUser.getGender())
                .set(USERS.BIRTHDATE, updatedUser.getBirthdate())
                .set(USERS.AVATAR, updatedUser.getAvatar())
                .set(USERS.AVATAR_ID, updatedUser.getAvatarId())
                .set(USERS.AVATAR_FRAME_ID, updatedUser.getAvatarFrameId())
                .where("id=?", updatedUser.getId())
                .execute();

        return rows > 0;
    }
}
