# sphinx-kotlin-core

Kotlin Multiplatform library for all the core sphinx features.

## TODO

Most of the core logic is in commonMain and we are using a few multiplatform dependency that allow us to get core functionality (serialization, db, models and the likes) across multiple platforms. We still don't have multiplatformx

- [Replace use of okhttp with a multiplatform dependency](https://github.com/stakwork/sphinx-kotlin-core/issues/1)
- [Replace the RSAImpl with a multiplatform dependency](https://github.com/stakwork/sphinx-kotlin-core/issues/2)
- [Make Implmementation classes internal](https://github.com/stakwork/sphinx-kotlin-core/issues/3)


The repo still has a dependency on OkHTTP which isn't yet a multiplatform libray.
