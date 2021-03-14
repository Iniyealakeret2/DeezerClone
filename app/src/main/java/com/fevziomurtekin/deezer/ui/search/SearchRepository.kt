package com.fevziomurtekin.deezer.ui.search

import com.fevziomurtekin.deezer.core.data.ApiResult
import com.fevziomurtekin.deezer.core.data.DaoResult
import com.fevziomurtekin.deezer.core.data.DataSource
import com.fevziomurtekin.deezer.core.extensions.getResult
import com.fevziomurtekin.deezer.core.extensions.isSuccessAndNotNull
import com.fevziomurtekin.deezer.core.extensions.letOnFalseOnSuspend
import com.fevziomurtekin.deezer.core.extensions.letOnTrueOnSuspend
import com.fevziomurtekin.deezer.core.extensions.isNotNull
import com.fevziomurtekin.deezer.data.SearchResponse
import com.fevziomurtekin.deezer.domain.local.DeezerDao
import com.fevziomurtekin.deezer.domain.network.DeezerClient
import com.fevziomurtekin.deezer.entities.SearchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val FAKE_DELAY_TIME = 1500L

class SearchRepository(
    private val deezerClient: DeezerClient,
    private val deezerDao: DeezerDao
): DataSource(), SearchRepositoryImpl {

    override fun fetchSearch(query:String) = flow {
        emit(ApiResult.Loading)
        localCallInsert {
            insertSearch(SearchEntity(q=query))
        }.data.isNotNull().letOnTrueOnSuspend {
            networkCall {
                deezerClient.fetchSearchAlbum(query)
            }.let { apiResult ->
                apiResult.isSuccessAndNotNull().letOnTrueOnSuspend {
                    emit(ApiResult.Success((apiResult.getResult() as SearchResponse).data))
                }.letOnFalseOnSuspend {
                    /* fake call */
                    delay(FAKE_DELAY_TIME)
                    emit(ApiResult.Error(Exception("Unexpected error.")))
                }
            }
        }.letOnFalseOnSuspend {
            /* fake call */
            delay(FAKE_DELAY_TIME)
            emit(ApiResult.Error(Exception("Unexpected error.")))
        }
    }.flowOn(Dispatchers.IO)

    override fun fetchRecentSearch()= flow {
        localCallFetch {
            deezerDao.getQueryList()
        }.let { localResult->
            localResult.isSuccessAndNotNull().letOnTrueOnSuspend {
                emit(ApiResult.Success(localResult.data as List<SearchEntity>))
            }.letOnFalseOnSuspend {
                emit(ApiResult.Error(Exception("Unexpected error.")))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun insertSearch(query: SearchEntity)= localCallInsert {
        deezerDao.insertQuery(query)
    }

}

interface SearchRepositoryImpl {

    /**
     * @return List<SearchQuery>?
     * */
    fun fetchRecentSearch() : Flow<ApiResult<List<*>>>

    /**
     * @param query, String
     * @return Result.Error or Result.Succes(List<SearchData>)
     * */
    fun fetchSearch(query:String) : Flow<ApiResult<List<*>>>

    /**
     * @param query,
     * insert the query.
     * */
    suspend fun insertSearch(query: SearchEntity): DaoResult

}
