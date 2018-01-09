# Auto-generated - DO NOT EDIT!
# To regenerate, edit openssl.config, then run:
#     ./import_openssl.sh import /path/to/openssl-1.1.0f.tar.gz
#
# This script will append to the following variables:
#
#    LOCAL_CFLAGS
#    LOCAL_C_INCLUDES
#    LOCAL_SRC_FILES_$(TARGET_ARCH)
#    LOCAL_SRC_FILES_$(TARGET_2ND_ARCH)
#    LOCAL_CFLAGS_$(TARGET_ARCH)
#    LOCAL_CFLAGS_$(TARGET_2ND_ARCH)
#    LOCAL_ADDITIONAL_DEPENDENCIES


LOCAL_ADDITIONAL_DEPENDENCIES += $(LOCAL_PATH)/Apps-config-target.mk

common_cflags := \
  -DMONOLITH \

common_src_files := \
  apps/app_rand.c \
  apps/apps.c \
  apps/asn1pars.c \
  apps/ca.c \
  apps/ciphers.c \
  apps/cms.c \
  apps/crl.c \
  apps/crl2p7.c \
  apps/dgst.c \
  apps/dh.c \
  apps/dhparam.c \
  apps/dsa.c \
  apps/dsaparam.c \
  apps/ec.c \
  apps/ecparam.c \
  apps/enc.c \
  apps/engine.c \
  apps/errstr.c \
  apps/gendh.c \
  apps/gendsa.c \
  apps/genpkey.c \
  apps/genrsa.c \
  apps/nseq.c \
  apps/ocsp.c \
  apps/openssl.c \
  apps/passwd.c \
  apps/pkcs12.c \
  apps/pkcs7.c \
  apps/pkcs8.c \
  apps/pkey.c \
  apps/pkeyparam.c \
  apps/pkeyutl.c \
  apps/prime.c \
  apps/rand.c \
  apps/req.c \
  apps/rsa.c \
  apps/rsautl.c \
  apps/s_cb.c \
  apps/s_client.c \
  apps/s_server.c \
  apps/s_socket.c \
  apps/s_time.c \
  apps/sess_id.c \
  apps/smime.c \
  apps/speed.c \
  apps/spkac.c \
  apps/srp.c \
  apps/verify.c \
  apps/version.c \
  apps/x509.c \

common_c_includes := \
  openssl/. \
  openssl/include \

arm_clang_asflags :=

arm_cflags :=

arm_src_files :=

arm_exclude_files :=

arm64_clang_asflags :=

arm64_cflags :=

arm64_src_files :=

arm64_exclude_files :=

x86_clang_asflags :=

x86_cflags :=

x86_src_files :=

x86_exclude_files :=

x86_64_clang_asflags :=

x86_64_cflags :=

x86_64_src_files :=

x86_64_exclude_files :=

mips_clang_asflags :=

mips_cflags :=

mips_src_files :=

mips_exclude_files :=

mips64_clang_asflags :=

mips64_cflags :=

mips64_src_files :=

mips64_exclude_files :=

mips32r6_clang_asflags :=

mips32r6_cflags :=

mips32r6_src_files :=

mips32r6_exclude_files :=


ifdef ARCH_MIPS_REV6
mips_cflags := $(mips32r6_cflags)
mips_src_files := $(mips32r6_src_files)
mips_exclude_files := $(mips32r6_exclude_files)
endif

LOCAL_CFLAGS += $(common_cflags)
LOCAL_C_INCLUDES += $(common_c_includes)

LOCAL_SRC_FILES_arm += $(filter-out $(arm_exclude_files),$(common_src_files) $(arm_src_files))
LOCAL_CFLAGS_arm += $(arm_cflags)
LOCAL_CLANG_ASFLAGS_arm += $(arm_clang_asflags)

LOCAL_SRC_FILES_arm64 += $(filter-out $(arm64_exclude_files),$(common_src_files) $(arm64_src_files))
LOCAL_CFLAGS_arm64 += $(arm64_cflags)
LOCAL_CLANG_ASFLAGS_arm64 += $(arm64_clang_asflags)

LOCAL_SRC_FILES_x86 += $(filter-out $(x86_exclude_files),$(common_src_files) $(x86_src_files))
LOCAL_CFLAGS_x86 += $(x86_cflags)
LOCAL_CLANG_ASFLAGS_x86 += $(x86_clang_asflags)

LOCAL_SRC_FILES_x86_64 += $(filter-out $(x86_64_exclude_files),$(common_src_files) $(x86_64_src_files))
LOCAL_CFLAGS_x86_64 += $(x86_64_cflags)
LOCAL_CLANG_ASFLAGS_x86_64 += $(x86_64_clang_asflags)

LOCAL_SRC_FILES_mips += $(filter-out $(mips_exclude_files),$(common_src_files) $(mips_src_files))
LOCAL_CFLAGS_mips += $(mips_cflags)
LOCAL_CLANG_ASFLAGS_mips += $(mips_clang_asflags)

LOCAL_SRC_FILES_mips64 += $(filter-out $(mips64_exclude_files),$(common_src_files) $(mips64_src_files))
LOCAL_CFLAGS_mips64 += $(mips64_cflags)
LOCAL_CLANG_ASFLAGS_mips64 += $(mips64_clang_asflags)
