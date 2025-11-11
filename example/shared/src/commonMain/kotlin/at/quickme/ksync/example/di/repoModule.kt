package at.quickme.ksync.example.di

import at.quickme.ksync.example.codegen.TodoRepoImpl
import org.koin.dsl.module
import at.quickme.ksync.example.repo.TodoRepo


val repoModule = module {
    single<TodoRepo> { TodoRepoImpl }
}