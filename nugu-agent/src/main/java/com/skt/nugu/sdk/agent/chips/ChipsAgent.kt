/**
 * Copyright (c) 2020 SK Telecom Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skt.nugu.sdk.agent.chips

import com.google.gson.JsonObject
import com.skt.nugu.sdk.agent.chips.handler.RenderDirectiveHandler
import com.skt.nugu.sdk.agent.version.Version
import com.skt.nugu.sdk.core.interfaces.capability.CapabilityAgent
import com.skt.nugu.sdk.core.interfaces.common.NamespaceAndName
import com.skt.nugu.sdk.core.interfaces.context.*
import com.skt.nugu.sdk.core.interfaces.directive.DirectiveSequencerInterface
import java.util.concurrent.CopyOnWriteArraySet

class ChipsAgent(
    directiveSequencer: DirectiveSequencerInterface,
    contextStateProviderRegistry: ContextStateProviderRegistry
) : CapabilityAgent
    , ChipsAgentInterface
    , SupportedInterfaceContextProvider
    , RenderDirectiveHandler.Renderer {
    companion object {
        const val NAMESPACE = "Chips"
        private val VERSION = Version(1, 0)

        private fun buildCompactContext(): JsonObject = JsonObject().apply {
            addProperty("version", VERSION.toString())
        }

        private val COMPACT_STATE: String = buildCompactContext().toString()
    }

    private val listeners = CopyOnWriteArraySet<ChipsAgentInterface.Listener>()

    init {
        contextStateProviderRegistry.setStateProvider(namespaceAndName, this)
        directiveSequencer.addDirectiveHandler(RenderDirectiveHandler(this))
    }

    override fun getInterfaceName(): String = NAMESPACE

    override fun provideState(
        contextSetter: ContextSetterInterface,
        namespaceAndName: NamespaceAndName,
        stateRequestToken: Int
    ) {
        contextSetter.setState(namespaceAndName, object : ContextState {
            override fun toFullJsonString(): String = COMPACT_STATE
            override fun toCompactJsonString(): String = COMPACT_STATE
        }, StateRefreshPolicy.NEVER, stateRequestToken)
    }

    override fun addListener(listener: ChipsAgentInterface.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ChipsAgentInterface.Listener) {
        listeners.remove(listener)
    }

    override fun render(directive: RenderDirective) {
        listeners.forEach {
            it.onReceiveChips(directive)
        }
    }
}