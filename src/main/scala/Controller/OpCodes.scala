
package Controller

object OpCodes
{
  //******************************************************************************//******************************************************************************

  //* MiniRISC CPU v2.0                                                          *
  //*                                                                            *
  //* Utasítás típusok:                                                          *
  //*            |15....12|11..........8|7......4|3...........0|                 *
  //* -A típusú: | opkód  | rX/vezérlés |   8 bites konstans   |                 *
  //* -B típusú: |  1111  | rX/vezérlés | opkód  | rY/vezérlés |                 *

  //* A 4'b1111 prefix jelzi, hogy a második operandus nem konstans, hanem       *
  //* regiszter, tehát B típusú utasításról van szó.                             *
  //******************************************************************************
  val REG_OP_PREFIX = 0xF //4'b1111


  //* MOV rX, addr                                                      A típusú *
  //* Adatmemória olvasás abszolút címzéssel: rX <- DMEM[addr]          - - - -  *

  //*  |15..12|11.....8|7................0|                                      *
  //*  | 1101 |   rX   | adatmemória cím  |                                      *

  //* MOV rX, (rY)                                                      B típusú *
  //* Adatmemória olvasás indirekt címzéssel: rX <- DMEM[rY]            - - - -  *

  //*  |15..12|11.....8|7......4|3.......0|                                      *
  //*  | 1111 |   rX   |  1101  |    rY   |                                      *

  val OPCODE_LD = 0xD //4'b1101


  //* MOV addr, rX                                                      A típusú *
  //* Adatmemória írás abszolút címzéssel: DMEM[addr] <- rX             - - - -  *


  //*  | 1001 |   rX   | adatmemória cím  |                                      *

  //* MOV (rY), rX                                                      B típusú *
  //* Adatmemória írás indirekt címzéssel: DMEM[rY] <- rX               - - - -  *


  //*  | 1111 |   rX   |  1001  |    rY   |                                      *

  val OPCODE_ST = 0xA // 4'b1001


  //* MOV rX, #imm                                                      A típusú *
  //* Konstans betöltése regiszterbe: rX <- imm                         - - - -  *


  //*  | 1100 |   rX   | 8 bites konstans |                                      *

  //* MOV rX, rY                                                        B típusú *
  //* Adatmozgatás regiszterbõl regiszterbe: rX <- rY                   - - - -  *


  //*  | 1111 |   rX   |  1100  |    rY   |                                      *

  val OPCODE_MOV = 0xC // 4'b1100


  //* ADD rX, #imm                                                      A típusú *
  //* Konstans hozzáadása regiszterhez átvitel nélkül: rX <- rX + imm   Z C N V  *

  //* ADC rX, #imm                                                      A típusú *
  //* Konstans hozzáadása regiszterhez átvitellel: rX <- rX + imm + C   Z C N V  *

  //* SUB rX, #imm                                                      A típusú *
  //* Konstans kivonása regiszterbõl átvitel nélkül: rX <- rX - imm     Z C N V  *

  //* SBC rX, #imm                                                      A típusú *
  //* Konstans kivonása regiszterbõl átvitellel: rX <- rX - imm + C     Z C N V  *


  //*  | 00SC |   rX   | 8 bites konstans |                                      *

  //* ADD rX, rY                                                        B típusú *
  //* Regiszter hozzáadása regiszterhez átvitel nélkül: rX <- rX + rY   Z C N V  *

  //* ADC rX, rY                                                        B típusú *
  //* Regiszter hozzáadása regiszterhez átvitellel: rX <- rX + rY + C   Z C N V  *

  //* SUB rX, rY                                                        B típusú *
  //* Regiszter kivonása regiszterbõl átvitel nélkül: rX <- rX - rY     Z C N V  *

  //* SBC rX, rY                                                        B típusú *
  //* Regiszter kivonása regiszterbõl átvitellel: rX <- rX - rY + C     Z C N V  *


  //*  | 1111 |   rX   |  00SC  |    rY   |                                      *

  //*  S: mûvelet kiválasztása (0: összeadás, 1: kivonás)                        *
  //*  C: átvitel kiválasztása (0: átvitel nélkül, 1: átvitellel)                *

  val OPCODE_ADD = 0x0 // 4'b0000
  val OPCODE_ADC = 0x1 // 4'b0001
  val OPCODE_SUB = 0x2 // 4'b0010
  val OPCODE_SBC = 0x3 // 4'b0011


  //* CMP rX, #imm                                                      A típusú *
  //* Regiszter összehasonlítása konstanssal: rX - imm                  Z C N V  *


  //*  | 1010 |   rX   | 8 bites konstans |                                      *

  //* CMP rX, rY                                                        B típusú *
  //* Regiszter összehasonlítása regiszterrel: rX - rY                  Z C N V  *


  //*  | 1111 |   rX   |  1010  |    rY   |                                      *

  val OPCODE_CMP = 0xA // 4'b1010


  //* AND rX, #imm                                                      A típusú *
  //* Bitenkénti ÉS konstanssal: rX <- rX & imm                         Z - N -  *

  //* OR  rX, #imm                                                      A típusú *
  //* Bitenkénti VAGY konstanssal: rX <- rX | imm                       Z - N -  *

  //* XOR rX, #imm                                                      A típusú *
  //* Bitenkénti XOR konstanssal: rX <- rX ^ imm                        Z - N -  *

  //* SWP rX                                                            A típusú *
  //* Alsó/felsõ 4 bit felcserélése: rX <- {rX[3:0], rX[7:4]}           Z - N -  *


  //*  | 01AB |   rX   | 8 bites konstans |                                      *

  //* AND rX, rY                                                        B típusú *
  //* Bitenkénti ÉS regiszterrel: rX <- rX & rY                         Z - N -  *

  //* OR  rX, rY                                                        B típusú *
  //* Bitenkénti VAGY regiszterrel: rX <- rX | rY                       Z - N -  *

  //* XOR rX, rY                                                        B típusú *
  //* Bitenkénti XOR regiszterrel: rX <- rX ^ rY                        Z - N -  *


  //*  | 1111 |   rX   |  01AB  |    rY   |                                      *

  //*  AB: mûvelet kiválasztása (00: ÉS, 01: VAGY, 10: XOR, 11: csere)           *

  //*  Megjegyzés: a csere mûvelet csak A típusú utasításnál értelmezett,        *
  //*  B típusú utasításnál ez a mûveleti kód shiftelést/forgatást hajt végre.   *

  val OPCODE_AND = 0x4 // 4'b0100
  val OPCODE_OR = 0x5 // 4'b0101
  val OPCODE_XOR = 0x6 // 4'b0110

  val LOGIC_AND = 0x0 // 2'b00
  val LOGIC_OR = 0x1 // 2'b01
  val LOGIC_XOR = 0x2 // 2'b10


  //* TST rX, #imm                                                      A típusú *
  //* Bittesztelés konstanssal: rX & imm                                Z - N -  *


  //*  | 1000 |   rX   | 8 bites konstans |                                      *

  //* TST rX, rY                                                        B típusú *
  //* Bittesztelés regiszterrel: rX & rY                                Z - N -  *


  //*  | 1111 |   rX   |  1000  |    rY   |                                      *

  val OPCODE_TST = 0x8 // 4'b1000


  //* SL0 rX                                                            B típusú *
  //* Shiftelés balra (0): rX <- {rX[6:0], 0}, C <- rX[7]               Z C N -  *

  //* SL1 rX                                                            B típusú *
  //* Shiftelés balra (1): rX <- {rX[6:0], 1}, C <- rX[7]               Z C N -  *

  //* SR0 rX                                                            B típusú *
  //* Shiftelés jobbra (0): rX <- {0, rX[7:1]}, C <- rX[0]              Z C N -  *

  //* SR1 rX                                                            B típusú *
  //* Shiftelés jobbra (1): rX <- {1, rX[7:1]}, C <- rX[0]              Z C N -  *

  //* ASR rX                                                            B típusú *
  //* Aritmetikai shift jobbra: rX <- {rX[7], rX[7:1]}, C <- rX[0]      Z C N -  *

  //* ROL rX                                                            B típusú *
  //* Forgatás balra: rX <- {rX[6:0], rX[7]}, C <- rX[7]                Z C N -  *

  //* ROR rX                                                            B típusú *
  //* Forgatás jobbra: rX <- {rX[0], rX[7:1]}, C <- rX[0]               Z C N -  *

  //* RLC rX                                                            B típusú *
  //* Forgatás balra carry-vel: rX <- {rX[6:0], C}, C <- rX[7]          Z C N -  *

  //* RRC rX                                                            B típusú *
  //* Forgatás jobbra carry-vel: rX <- {C, rX[7:1]}, C <- rX[0]         Z C N -  *


  //*  | 1111 |   rX   |  0111  |  AIRD   |                                      *

  //*  D: irány kiválasztása (0: balra, 1: jobbra)                               *
  //*  R: mûvelet kiválasztása (0: shiftelés, 1: forgatás)                       *
  //*  I: a beshiftelt bit értéke/kiválasztása (0: 0/kishiftelt bit, 1: 1/carry) *
  //*  A: a shiftelés típusa (0: normál, 1: aritmetikai)                         *

  val OPCODE_SHIFT = 0x7 // 4'b0111

  val SHIFT_SHL = 0x0 // 2'b00
  val SHIFT_SHR = 0x1 // 2'b01
  val SHIFT_ROL = 0x2 // 2'b10
  val SHIFT_ROR = 0x3 // 2'b11


  //* JMP addr - Feltétel nélküli ugrás (PC <- addr)                    A típusú *
  //* JZ  addr - Ugrás, ha a Z flag 1   (PC <- addr, ha Z=1)            - - - -  *
  //* JNZ addr - Ugrás, ha a Z flag 0   (PC <- addr, ha Z=0)                     *
  //* JC  addr - Ugrás, ha a C flag 1   (PC <- addr, ha C=1)                     *
  //* JNC addr - Ugrás, ha a C flag 0   (PC <- addr, ha C=0)                     *
  //* JN  addr - Ugrás, ha az N flag 1  (PC <- addr, ha N=1)                     *
  //* JNN addr - Ugrás, ha az N flag 0  (PC <- addr, ha N=0)                     *
  //* JV  addr - Ugrás, ha a V flag 1   (PC <- addr, ha V=1)                     *
  //* JNV addr - Ugrás, ha a V flag 0   (PC <- addr, ha V=0)                     *
  //* JSR addr - Szubrutinhívás         (stack <- PC <- addr)                    *


  //*  | 1011 | mûvelet|programmemória cím|                                      *

  //* JMP (rY) - Feltétel nélküli ugrás (PC <- rY)                      B típusú *
  //* JZ  (rY) - Ugrás, ha a Z flag 1   (PC <- rY, ha Z=1)              - - - -  *
  //* JNZ (rY) - Ugrás, ha a Z flag 0   (PC <- rY, ha Z=0)                       *
  //* JC  (rY) - Ugrás, ha a C flag 1   (PC <- rY, ha C=1)                       *
  //* JNC (rY) - Ugrás, ha a C flag 0   (PC <- rY, ha C=0)                       *
  //* JN  (rY) - Ugrás, ha az N flag 1  (PC <- rY, ha N=1)                       *
  //* JNN (rY) - Ugrás, ha az N flag 0  (PC <- rY, ha N=0)                       *
  //* JV  (rY) - Ugrás, ha a V flag 1   (PC <- rY, ha V=1)                       *
  //* JNV (rY) - Ugrás, ha a V flag 0   (PC <- rY, ha V=0)                       *
  //* JSR (rY) - Szubrutinhívás         (stack <- PC <- rY)                      *


  //*  | 1111 | mûvelet|  1011  |    rY   |                                      *

  //* RTS - Visszatérés szubrutinból    (PC <- stack)                   A típusú *
  //* RTI - Visszatérés megszakításból  (PC,Z,C,N,V,IE <- stack)        - - - -  *
  //* CLI - Megszakítások tiltása       (IE <- 0)                                *
  //* STI - Megszakítások engedélyezése (IE <- 1)                                *


  //*  | 1011 | mûvelet|     00000000     |                                      *

  val OPCODE_CTRL = 0xB // 4'b1011

  val CTRL_JMP = 0x0 // 4'b0000
  val CTRL_JZ = 0x1 // 4'b0001
  val CTRL_JNZ = 0x2 // 4'b0010
  val CTRL_JC = 0x3 // 4'b0011
  val CTRL_JNC = 0x4 // 4'b0100
  val CTRL_JN = 0x5 // 4'b0101
  val CTRL_JNN = 0x6 // 4'b0110
  val CTRL_JV = 0x7 // 4'b0111
  val CTRL_JNV = 0x8 // 4'b1000
  val CTRL_JSR = 0x9 // 4'b1001
  val CTRL_RTS = 0xA // 4'b1010
  val CTRL_RTI = 0xB // 4'b1011
  val CTRL_CLI = 0xC // 4'b1100
  val CTRL_STI = 0xD // 4'b1101
}
