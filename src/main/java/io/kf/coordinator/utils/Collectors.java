/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.kf.coordinator.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Collectors {

  public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
    return Collector.of(
        ImmutableList.Builder::new,
        (builder, e) -> builder.add(e),
        (b1, b2) -> b1.addAll(b2.build()),
        (builder) -> builder.build());
  }

  public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
    return Collector.of(
        ImmutableSet.Builder::new,
        (builder, e) -> builder.add(e),
        (b1, b2) -> b1.addAll(b2.build()),
        (builder) -> builder.build());
  }

  public static <T, K, V> Collector<T, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> toImmutableMap(
      @NonNull Function<? super T, ? extends K> keyMapper, @NonNull Function<? super T, ? extends V> valueMapper) {

    final BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator =
        (builder, entry) -> builder.put(keyMapper.apply(entry), valueMapper.apply(entry));

    return Collector.of(
        ImmutableMap.Builder::new,
        accumulator,
        (b1, b2) -> b1.putAll(b2.build()),
        ImmutableMap.Builder::build);
  }

  public static <T, K> Collector<T, ImmutableMap.Builder<K, T>, ImmutableMap<K, T>> toImmutableMap(
      @NonNull Function<? super T, ? extends K> keyMapper) {
    return toImmutableMap(keyMapper, Function.identity());
  }

}
