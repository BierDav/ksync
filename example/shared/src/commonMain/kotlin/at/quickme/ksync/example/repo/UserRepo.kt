package at.quickme.ksync.at.quickme.ksync.example.repo

import at.quickme.ksync.at.quickme.ksync.example.entity.User
import at.quickme.ksync.at.quickme.ksync.example.entity.UserMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(UserMapper::class)
interface UserRepo : CrudRepository<User>