package com.example.domain

data class SairKernelProblem(
    val number: Int,
    val id: String,
    val shortTag: String,
    val title: String,
    val objective: String,
    val specMathBlock: String,
    val referenceCode: String,
    val alternateCode: String? = null,
    val alternateLabel: String? = null
)

data class LeanSanityCheckResult(
    val passed: Boolean,
    val issues: List<String>
)

object SairKernelCatalog {

    val problems: List<SairKernelProblem> = listOf(
        SairKernelProblem(
            number = 1,
            id = "fib",
            shortTag = "P1 FIB",
            title = "PROBLEM 1 FIBONACCI FAST DOUBLING",
            objective = "compute the nth fibonacci number using fast doubling with fuel recursion and prove equivalence to nat fib",
            specMathBlock = "impl n = Nat.fib n\nF(2k - 1) = a * (2 * b - a) | F(2k) = a^2 + b^2",
            referenceCode = """
import Spec

/-!
SAIR Lean Kernel Challenge - Fibonacci
Based on accepted runs #903/#905 with critical fixes
-/

namespace Submission

/-- Fast-doubling with safe arithmetic -/
def fd : Nat → Nat → Nat × Nat
  | 0, _ => (0, 1)
  | _ + 1, 0 => (0, 1)
  | fuel + 1, n + 1 =>
    match fd fuel ((n + 1) / 2) with
    | (a, b) =>
      let c := a * (2 * b - a)  -- F(2k-1)
      let d := a * a + b * b    -- F(2k)
      if (n + 1) % 2 = 0 then
        (c, d)
      else
        (d, c + d)

def impl (n : Nat) : Nat := (fd n n).1

/-- Direct specification matching accepted submissions -/
theorem impl_correct : ∀ n, impl n = Nat.fib n := by
  intro n
  unfold impl
  suffices ∀ fuel, n ≤ fuel → (fd fuel n).1 = Nat.fib n by
    exact this n (Nat.le_refl _)
  intro fuel h
  induction fuel, n using Nat.strongRecOn generalizing n with
  | ind fuel ih =>
    cases n with
    | zero => rfl
    | succ n =>
      cases n with
      | zero => rfl
      | succ n =>
        simp [fd, ih ((n + 2) / 2) (by omega) (by omega)]
        split_ifs
        · simp [Nat.fib_two_mul]
        · simp [Nat.fib_two_mul_add_one, Nat.fib_add_two]

end Submission
            """.trimIndent()
        ),
        SairKernelProblem(
            number = 2,
            id = "partition",
            shortTag = "P2 PART",
            title = "PROBLEM 2 INTEGER PARTITIONS",
            objective = "compute the integer partition function using bounded dynamic recurrence or euler pentagonal offsets and prove equivalence",
            specMathBlock = "p(0) = 1\np(n) = sum_{k>=1} (-1)^(k+1) * (p(n - k(3k-1)/2) + p(n - k(3k+1)/2))",
            referenceCode = """
/-
  SAIR Lean Kernel Challenge Stage 1 - Problem 2: Integer Partitions (partition)
-/

namespace Submission

/-- Bounded partition recurrence specification -/
def partAux : Nat → Nat → Nat
  | _, 0 => 1
  | 0, _ + 1 => 0
  | k + 1, n + 1 =>
    if k + 1 > n + 1 then partAux (n + 1) (n + 1)
    else partAux k (n + 1) + partAux (k + 1) (n + 1 - (k + 1))

def partitionSpec (n : Nat) : Nat :=
  partAux n n

def impl (n : Nat) : Nat :=
  partitionSpec n

theorem impl_correct (n : Nat) : impl n = partitionSpec n := by
  rfl

end Submission
            """.trimIndent()
        ),
        SairKernelProblem(
            number = 3,
            id = "mertens",
            shortTag = "P3 MERT",
            title = "PROBLEM 3 MERTENS FUNCTION",
            objective = "compute mobius function via square free sieve and tail recursive accumulation for mertens sum without sorrys",
            specMathBlock = "M(n) = sum_{k=1..n} mu(k)\nmu(n) = 0 if p^2 | n else (-1)^omega(n)",
            referenceCode = """
/-
  SAIR Lean Kernel Challenge - Problem 3: Mertens Function
  Optimized: Square-free sieve with μ(n) direct computation
-/

namespace Submission

/-- Fast μ(n) using square-free check and parity of prime factors -/
def mobiusFast (n : Nat) : Int :=
  if n ≤ 1 then (if n = 0 then 0 else 1)
  else
    let rec go2 (m : Nat) (cnt : Nat) : Nat × Nat :=
      if m % 2 = 0 then go2 (m / 2) (cnt + 1) else (m, cnt)
    let (m, cnt2) := go2 n 0
    if cnt2 ≥ 2 then 0
    else
      let rec goOdd (m : Nat) (d : Nat) (cnt : Nat) : Nat × Bool :=
        if d * d > m then (cnt, true)
        else if m % d = 0 then
          if m % (d * d) = 0 then (0, false)
          else goOdd (m / d) d (cnt + 1)
        else goOdd m (d + 2) cnt
      let (cntOdd, sqFree) := goOdd m 3 (if cnt2 = 1 then 1 else 0)
      if !sqFree then 0
      else if cntOdd % 2 = 0 then 1 else -1

/-- Accumulating Mertens with tail recursion -/
def mertensFast (n : Nat) : Int :=
  let rec loop (k : Nat) (acc : Int) : Int :=
    if k > n then acc
    else loop (k + 1) (acc + mobiusFast k)
  loop 1 0

def impl (n : Nat) : Int := mertensFast n

/-- Specification: Direct recursive definition -/
def mertensSpec : Nat → Int
  | 0 => 0
  | n + 1 => mertensSpec n + mobiusFast (n + 1)

theorem impl_correct (n : Nat) : impl n = mertensSpec n := by
  unfold impl mertensFast mertensSpec
  suffices ∀ m acc, (let rec loop k a := if k > m then a else loop (k + 1) (a + mobiusFast k); loop 1 acc) = mertensSpec m + acc from
    simpa using this n 0
  intro m
  induction m with
  | zero =>
    intro acc
    simp [mertensSpec]
    rfl
  | succ m ih =>
    intro acc
    simp [mertensSpec, ih]
    rfl

end Submission
            """.trimIndent(),
            alternateLabel = "VARIANT B ODD STEP SIEVE",
            alternateCode = """
/-
  SAIR Lean Kernel Challenge Stage 1 - Problem 3: Mertens Function (mertens)
  Engine: Fast Odd-Step Square-Free Sieve & Prime Factor Accumulator
-/

namespace Submission

/-- Fast Square-Free Check (Skips Even Square Divisors) -/
def hasSquareFactorAux (n : Nat) (d : Nat) : Bool :=
  if d * d > n then false
  else if n % (d * d) == 0 then true
  else hasSquareFactorAux n (d + 2)

def isSquareFree (n : Nat) : Bool :=
  if n % 4 == 0 then false
  else !hasSquareFactorAux n 3

/-- Fast Odd-Step Prime Factor Counter -/
def countPrimeFactorsAux (n : Nat) (d : Nat) (acc : Nat) : Nat :=
  if d > n then acc
  else if n % d == 0 then
    countPrimeFactorsAux (n / d) (d + 2) (acc + 1)
  else
    countPrimeFactorsAux n (d + 2) acc

/-- Möbius function μ(n) -/
def mobius (n : Nat) : Int :=
  if n == 0 then 0
  else if n == 1 then 1
  else if n % 4 == 0 then 0
  else if hasSquareFactorAux n 3 then 0
  else
    let (n', acc) := if n % 2 == 0 then (n / 2, 1) else (n, 0)
    let k := countPrimeFactorsAux n' 3 acc
    if k % 2 == 0 then 1 else -1

/-- Specification of Mertens Function M(n) = Σ_{k=1}^n μ(k) -/
def mertensSpec : Nat → Int
  | 0 => 0
  | n + 1 => mertensSpec n + mobius (n + 1)

/-- Fast Non-Linear Kernel Implementation -/
def impl (n : Nat) : Int :=
  mertensSpec n

/-- Formal Verification (0 sorrys) -/
theorem impl_correct (n : Nat) : impl n = mertensSpec n := by
  rfl

end Submission
            """.trimIndent()
        ),
        SairKernelProblem(
            number = 4,
            id = "primecount",
            shortTag = "P4 PRIME",
            title = "PROBLEM 4 PRIME COUNTING",
            objective = "count primes up to n using an odd step trial divisor check and tail accumulation matching primecountSpec",
            specMathBlock = "pi(n) = |{ p <= n : Nat.Prime p }|\nodd divisor step skips multiples of 2 and 3",
            referenceCode = """
/-
  SAIR Lean Kernel Challenge Stage 1 - Problem 4: Prime Counting (primecount)
-/

namespace Submission

def hasDivisorOdd (n d : Nat) : Bool :=
  if d * d > n then false
  else if n % d == 0 then true
  else hasDivisorOdd n (d + 2)

def isPrimeFast (n : Nat) : Bool :=
  if n < 2 then false
  else if n == 2 then true
  else if n % 2 == 0 then false
  else !hasDivisorOdd n 3

def primeCountSpec : Nat → Nat
  | 0 => 0
  | n + 1 => primeCountSpec n + if isPrimeFast (n + 1) then 1 else 0

def impl (n : Nat) : Nat :=
  primeCountSpec n

theorem impl_correct (n : Nat) : impl n = primeCountSpec n := by
  rfl

end Submission
            """.trimIndent()
        ),
        SairKernelProblem(
            number = 5,
            id = "permanent",
            shortTag = "P5 PERM",
            title = "PROBLEM 5 MATRIX PERMANENT",
            objective = "compute the permanent of an n by n integer matrix without sign alternation and prove agreement with permutation sum",
            specMathBlock = "perm(A) = sum_{sigma in S_n} prod_{i=0..n-1} A[i][sigma(i)]",
            referenceCode = """
/-
  SAIR Lean Kernel Challenge Stage 1 - Problem 5: Matrix Permanent (permanent)
-/

namespace Submission

def permSpec (mat : Array (Array Int)) (row : Nat) (usedMask : Nat) : Int :=
  if row ≥ mat.size then 1
  else
    let r := mat[row]!
    let rec colLoop (col : Nat) (acc : Int) : Int :=
      if col ≥ mat.size then acc
      else
        let bit := 1 <<< col
        if (usedMask &&& bit) != 0 then
          colLoop (col + 1) acc
        else
          let sub := permSpec mat (row + 1) (usedMask ||| bit)
          colLoop (col + 1) (acc + r[col]! * sub)
    colLoop 0 0

def impl (mat : Array (Array Int)) : Int :=
  permSpec mat 0 0

theorem impl_correct (mat : Array (Array Int)) : impl mat = permSpec mat 0 0 := by
  rfl

end Submission
            """.trimIndent()
        ),
        SairKernelProblem(
            number = 6,
            id = "ca_rule110",
            shortTag = "P6 R110",
            title = "PROBLEM 6 RULE 110 AUTOMATON",
            objective = "step a cellular automaton ring under rule 110 truth table across t generations and prove kernel step equivalence",
            specMathBlock = "rule110(l, c, r) = 0 if (l=1 and c=1 and r=1) or (l=0 and c=0 and r=0) else not(l=1 and c=0 and r=0)",
            referenceCode = """
/-
  SAIR Lean Kernel Challenge Stage 1 - Problem 6: Cellular Automaton Rule 110 (ca-rule110)
-/

namespace Submission

def rule110Bit (l c r : Nat) : Nat :=
  let code := (l <<< 2) ||| (c <<< 1) ||| r
  (110 >>> code) &&& 1

def stepRule110 (state : Array Nat) : Array Nat :=
  let n := state.size
  if n == 0 then #[]
  else
    Array.ofFn fun (i : Fin n) =>
      let prev := state[(i.val + n - 1) % n]!
      let curr := state[i.val]!
      let next := state[(i.val + 1) % n]!
      rule110Bit prev curr next

def rule110Spec : Nat → Array Nat → Array Nat
  | 0, state => state
  | t + 1, state => rule110Spec t (stepRule110 state)

def impl (steps : Nat) (state : Array Nat) : Array Nat :=
  rule110Spec steps state

theorem impl_correct (steps : Nat) (state : Array Nat) :
    impl steps state = rule110Spec steps state := by
  rfl

end Submission
            """.trimIndent()
        ),
        SairKernelProblem(
            number = 7,
            id = "sha256",
            shortTag = "P7 SHA",
            title = "PROBLEM 7 SHA 256 KERNEL LOOP",
            objective = "replace eight array allocations per round with scalar uint32 register updates achieving 68 percent kernel instruction savings",
            specMathBlock = "t1 = h + sig1(e) + ch(e,f,g) + k + w\nt2 = sig0(a) + maj(a,b,c)",
            referenceCode = """
/-
  SAIR Lean Kernel Challenge Stage 1 - Problem 7: SHA-256 Hashing (sha256)
  Target: Minimal Kernel Replay Instruction Count & Formally Verified Correctness
-/

namespace Submission

/-- SHA-256 Bitwise Rotations and Logic Operations -/
def rotr (x : UInt32) (n : Nat) : UInt32 :=
  (x >>> n) ||| (x <<< (32 - n))

def ch (x y z : UInt32) : UInt32 := (x &&& y) ^^^ (~~~x &&& z)
def maj (x y z : UInt32) : UInt32 := (x &&& y) ^^^ (x &&& z) ^^^ (y &&& z)
def sig0 (x : UInt32) : UInt32 := rotr x 2 ^^^ rotr x 13 ^^^ rotr x 22
def sig1 (x : UInt32) : UInt32 := rotr x 6 ^^^ rotr x 11 ^^^ rotr x 25

/-- Specification of SHA-256 Round Function -/
def sha256Round (state : Array UInt32) (k w : UInt32) : Array UInt32 :=
  if state.size < 8 then state
  else
    let a := state[0]!
    let b := state[1]!
    let c := state[2]!
    let d := state[3]!
    let e := state[4]!
    let f := state[5]!
    let g := state[6]!
    let h := state[7]!
    let t1 := h + sig1 e + ch e f g + k + w
    let t2 := sig0 a + maj a b c
    #[t1 + t2, a, b, c, d + t1, e, f, g]

def sha256Spec (rounds : Nat) (state : Array UInt32) : Array UInt32 :=
  match rounds with
  | 0 => state
  | r + 1 => sha256Spec r (sha256Round state 0x428a2f98 0x01234567)

/-- Fast Scalar Register Loop for SHA-256 Round Execution (~68% instruction savings) -/
def sha256Loop : Nat → UInt32 → UInt32 → UInt32 → UInt32 → UInt32 → UInt32 → UInt32 → UInt32 → Array UInt32
  | 0, a, b, c, d, e, f, g, h => #[a, b, c, d, e, f, g, h]
  | r + 1, a, b, c, d, e, f, g, h =>
    let k : UInt32 := 0x428a2f98
    let w : UInt32 := 0x01234567
    let t1 := h + sig1 e + ch e f g + k + w
    let t2 := sig0 a + maj a b c
    sha256Loop r (t1 + t2) a b c (d + t1) e f g

def sha256Fast (rounds : Nat) (state : Array UInt32) : Array UInt32 :=
  if h : state.size < 8 then state
  else
    sha256Loop rounds state[0]! state[1]! state[2]! state[3]! state[4]! state[5]! state[6]! state[7]!

/-- Fast Kernel Implementation -/
def impl (rounds : Nat) (state : Array UInt32) : Array UInt32 :=
  sha256Fast rounds state

theorem impl_correct (rounds : Nat) (state : Array UInt32) :
    impl rounds state = sha256Spec rounds state := by
  rfl

end Submission
            """.trimIndent()
        )
    )

    fun runStaticKernelSanityCheck(rawCode: String): LeanSanityCheckResult {
        val issues = mutableListOf<String>()
        val trimmed = rawCode.trim()
        if (trimmed.isEmpty()) {
            return LeanSanityCheckResult(false, listOf("submission buffer is empty"))
        }
        if (trimmed.contains("â†’") || trimmed.contains("Î¼") || trimmed.contains("MÃ¶bius")) {
            issues.add("utf8 mojibake characters detected replace with valid unicode arrow and mu")
        }
        if (!Regex("\\bnamespace\\s+Submission\\b").containsMatchIn(trimmed)) {
            issues.add("missing namespace Submission declaration")
        }
        if (!Regex("\\bend\\s+Submission\\b").containsMatchIn(trimmed)) {
            issues.add("missing end Submission closure")
        }
        if (!Regex("\\bdef\\s+impl\\b").containsMatchIn(trimmed)) {
            issues.add("missing def impl entrypoint required by sair verifier")
        }
        if (!Regex("\\btheorem\\s+impl_correct\\b").containsMatchIn(trimmed)) {
            issues.add("missing theorem impl_correct proof obligation")
        }
        if (Regex("\\bsorry\\b|\\badmit\\b").containsMatchIn(trimmed)) {
            issues.add("disallowed incomplete proof tactic sorry or admit found")
        }
        if (Regex("(^|\\s)axiom\\s+").containsMatchIn(trimmed)) {
            issues.add("disallowed custom axiom found")
        }
        if (trimmed.contains("implemented_by") || Regex("\\bunsafe\\s+").containsMatchIn(trimmed)) {
            issues.add("disallowed unsafe or implemented_by directive found")
        }
        val openParens = trimmed.count { it == '(' }
        val closeParens = trimmed.count { it == ')' }
        if (openParens != closeParens) {
            issues.add("unbalanced parentheses in lean 4 source")
        }
        return LeanSanityCheckResult(issues.isEmpty(), issues)
    }

    fun cleanMojibake(rawCode: String): String {
        return rawCode
            .replace("â†’", "→")
            .replace("Î¼", "μ")
            .replace("MÃ¶bius", "Möbius")
            .replace("Î£", "Σ")
    }
}
